package com.bmad.service;

import com.bmad.model.DocumentSegment;
import com.bmad.nativeapi.LlamaNative;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lucene 索引服务类。
 * 
 * 职责：
 * 1. 负责管理本地向量检索和全文检索的索引库。
 * 2. 这里的实现使用 Lucene 9.x 的 KnnVectorField 支持 HNSW 向量索引。
 * 3. 实现了混合检索 (Hybrid Search)，结合向量相似度和关键词匹配。
 */
@Service
public class IndexService {

    private static final String INDEX_PATH = "lucene_index";
    private final Analyzer analyzer = new StandardAnalyzer();
    private static final int VECTOR_DIM = 128; // Keep 128 as it matches the mock native implementation for now

    private Directory directory;
    private IndexWriter writer;

    @PostConstruct
    public void init() throws IOException {
        directory = FSDirectory.open(Paths.get(INDEX_PATH));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Lucene 9.x 会自动管理 Write Lock，如果索引库被异常锁定，IndexWriter 构造时会抛出 LockObtainFailedException
        writer = new IndexWriter(directory, config);
        System.out.println("[IndexService] 索引服务初始化完成。");
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (directory != null) {
            directory.close();
        }
    }

    /**
     * 为文档建立索引。
     * 支持 Parent-Child Indexing 和 元数据存储。
     */
    public void indexDocument(String filename, List<DocumentSegment> segments) throws IOException {
        System.out.println("[IndexService] 正在为文件建立索引: " + filename + " (分段数: " + segments.size() + ")");
        
        int totalChildCount = 0;
        Set<String> reservedFields = new HashSet<>(Arrays.asList("filename", "parent_id", "content", "child_content", "chunk_id", "vector", "image_data", "anchor"));
        
        for (int segIdx = 0; segIdx < segments.size(); segIdx++) {
            DocumentSegment segment = segments.get(segIdx);
            String content = segment.getContent();
            Map<String, Object> metadata = segment.getMetadata();
            
            // 1. 父级切片 (Parent Chunks): 较大粒度，作为返回给 LLM 的上下文 (e.g. 800-1000 chars)
            List<String> parentChunks = adaptiveChunking(content, 800, 100);
            
            for (int pIdx = 0; pIdx < parentChunks.size(); pIdx++) {
                String parentChunk = parentChunks.get(pIdx);
                // Unique ID for Parent Chunk: filename + segment + parent_idx
                String parentId = filename + "#s" + segIdx + "p" + pIdx; 
                
                // 2. 子级切片 (Child Chunks): 较小粒度，用于生成向量索引 (e.g. 200-300 chars)
                List<String> childChunks = adaptiveChunking(parentChunk, 300, 50);
                
                for (int cIdx = 0; cIdx < childChunks.size(); cIdx++) {
                    String childChunk = childChunks.get(cIdx);
                    Document doc = new Document();
                    
                    // 存储字段
                    doc.add(new TextField("filename", filename, Field.Store.YES)); // Change to TextField for keyword search
                    doc.add(new StringField("parent_id", parentId, Field.Store.YES));
                    doc.add(new TextField("content", parentChunk, Field.Store.YES)); // Parent Chunk Text (Stored)
                    doc.add(new TextField("child_content", childChunk, Field.Store.YES)); // Child Chunk Text (Stored, Indexed)
                    doc.add(new IntField("chunk_id", cIdx, Field.Store.YES));
                    
                    // 存储图片数据 (如果有)
                    String imageData = segment.getImageData();
                    if (imageData != null) {
                        doc.add(new StoredField("image_data", imageData));
                    }
                    
                    // 存储元数据
                    if (metadata != null) {
                        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                            String key = entry.getKey();
                            if (reservedFields.contains(key)) continue; // Skip reserved fields to avoid type conflict
                            Object value = entry.getValue();
                            if (value != null) {
                                doc.add(new StringField(key, value.toString(), Field.Store.YES));
                            }
                        }
                        String anchor = buildAnchor(metadata);
                        if (anchor != null) {
                            doc.add(new StringField("anchor", anchor, Field.Store.YES));
                        }
                    }
                    
                    // 向量字段 (基于 Child Chunk)
                    float[] vector = LlamaNative.getEmbedding(childChunk);
                    if (vector != null) {
                        doc.add(new KnnVectorField("vector", vector, VectorSimilarityFunction.COSINE));
                    }
                    
                    writer.addDocument(doc);
                    totalChildCount++;
                }
            }
        }
        writer.commit();
        System.out.println("[IndexService] 索引提交成功，共写入子分块数: " + totalChildCount);
    }

    /**
     * 清空所有索引数据。
     */
    public void deleteAll() throws IOException {
        writer.deleteAll();
        writer.commit();
        System.out.println("[IndexService] 索引已清空。");
    }

    private String buildAnchor(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }

        Object pageNumber = metadata.get("page_number");
        if (pageNumber != null) {
            return "Page " + pageNumber;
        }

        Object slideNumber = metadata.get("slide_number");
        if (slideNumber != null) {
            return "Slide " + slideNumber;
        }

        Object paragraphIndex = metadata.get("paragraph_index");
        if (paragraphIndex != null) {
            return "Paragraph " + paragraphIndex;
        }

        return null;
    }

    /**
     * 自适应分块算法。
     * 1. 优先按段落分割。
     * 2. 段落过长则按句子分割。
     * 3. 保证最大长度限制并包含重叠。
     */
    private List<String> adaptiveChunking(String text, int maxChars, int overlap) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;

        // 1. 按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        
        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty()) continue;

            if (para.length() <= maxChars) {
                result.add(para);
            } else {
                // 2. 段落过长，按句子分割
                List<String> sentences = splitIntoSentences(para);
                StringBuilder currentChunk = new StringBuilder();
                
                for (String sentence : sentences) {
                    if (currentChunk.length() + sentence.length() > maxChars && currentChunk.length() > 0) {
                        result.add(currentChunk.toString().trim());
                        // 保留部分重叠
                        int overlapStart = Math.max(0, currentChunk.length() - overlap);
                        String overlapText = currentChunk.substring(overlapStart);
                        currentChunk = new StringBuilder(overlapText);
                    }
                    currentChunk.append(sentence).append(" ");
                }
                
                if (currentChunk.length() > 0) {
                    result.add(currentChunk.toString().trim());
                }
            }
        }
        return result;
    }

    /**
     * 简单的句子分割逻辑。
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        // 匹配 . ! ? 后面跟随空格或换行的位置
        String[] parts = text.split("(?<=[。！？.!?.])\\s+");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                sentences.add(part.trim());
            }
        }
        return sentences;
    }

    /**
     * 混合检索 (Hybrid Search)
     * 结合向量相似度 (Semantic) 和关键词匹配 (Lexical)。
     * 
     * @param queryString 用户查询
     * @return 包含文档内容、文件名和相关度得分的结果列表
     */
    public List<Map<String, Object>> search(String queryString) throws Exception {
        System.out.println("[IndexService] 收到检索请求: " + queryString);
        // 搜索时需要打开一个新的 DirectoryReader 以获取最新索引
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            
            BooleanQuery.Builder builder = new BooleanQuery.Builder();

            // 1. 向量检索 (Semantic Search)
            // 只有当 LlamaNative 返回有效向量时才添加向量查询
            float[] queryVector = LlamaNative.getEmbedding(queryString);
            if (queryVector != null && queryVector.length > 0) {
                // KnnFloatVectorQuery is for Lucene 9.5+, checking current Lucene version capabilities
                // For older Lucene 9.x, it might be KnnVectorQuery
                Query vectorQuery = new KnnFloatVectorQuery("vector", queryVector, 20);
                builder.add(vectorQuery, BooleanClause.Occur.SHOULD);
                System.out.println("[IndexService] 已添加向量检索条件");
            }
            
            // 2. 关键词检索 (Keyword Search)
            // 同时在 content, child_content 和 filename 字段进行关键词检索
            String[] fields = {"content", "child_content", "filename"};
            Map<String, Float> boosts = new HashMap<>();
            boosts.put("filename", 5.0f);      // 文件名匹配权重最高
            boosts.put("content", 1.0f);
            boosts.put("child_content", 1.0f);
            
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
            // 默认操作符设为 OR，提高召回率
            parser.setDefaultOperator(QueryParser.Operator.OR);
            // 允许关键词以 * 开头，支持更灵活的模糊匹配
            parser.setAllowLeadingWildcard(true);
            
            try {
                // 预处理查询字符串，如果是单个词且不含通配符，尝试前后模糊匹配
                String escapedQuery = QueryParser.escape(queryString);
                Query keywordQuery;
                if (!queryString.contains(" ") && !queryString.contains("*") && !queryString.contains("?")) {
                    // 对于单词搜索，增加通配符支持以提高文件名匹配成功率 (例如输入 cat 匹配 cat.jpg)
                    String wildcardQueryStr = "*" + escapedQuery + "*";
                    keywordQuery = parser.parse(wildcardQueryStr);
                    System.out.println("[IndexService] 使用通配符检索: " + wildcardQueryStr);
                } else {
                    keywordQuery = parser.parse(escapedQuery);
                }
                
                // 提高关键词检索的权重
                Query boostedKeywordQuery = new BoostQuery(keywordQuery, 2.0f);
                builder.add(boostedKeywordQuery, BooleanClause.Occur.SHOULD);
                System.out.println("[IndexService] 已添加关键词检索条件: " + keywordQuery.toString());
            } catch (Exception e) {
                System.err.println("[IndexService] 关键词解析失败: " + e.getMessage());
            }
            
            BooleanQuery hybridQuery = builder.build();
            System.out.println("[IndexService] 最终 Query: " + hybridQuery.toString());
            
            // 如果两个查询都为空，直接返回空
            if (hybridQuery.clauses().isEmpty()) {
                return new ArrayList<>();
            }

            // 4. 执行检索
            TopDocs docs = searcher.search(hybridQuery, 20);
            System.out.println("[IndexService] 检索完成，命中文档数: " + docs.totalHits.value);
            
            List<Map<String, Object>> results = new ArrayList<>();
            Set<String> seenParentIds = new HashSet<>();
            
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String parentId = doc.get("parent_id");
                
                // Deduplicate by Parent Chunk ID
                if (parentId != null && seenParentIds.contains(parentId)) {
                    continue;
                }
                if (parentId != null) {
                    seenParentIds.add(parentId);
                }
                
                Map<String, Object> item = new HashMap<>();
                item.put("score", scoreDoc.score);
                
                // Extract all stored fields
                for (IndexableField field : doc.getFields()) {
                    if (field.fieldType().stored()) {
                        String val = field.stringValue();
                        if (val != null) {
                            item.put(field.name(), val);
                        } else if (field.numericValue() != null) {
                            item.put(field.name(), field.numericValue());
                        }
                    }
                }
                
                // 打印调试信息：检查是否包含图片数据
                if (item.containsKey("image_data")) {
                    String img = (String) item.get("image_data");
                    System.out.println("[IndexService] 结果包含图片数据，长度: " + img.length());
                } else {
                    System.out.println("[IndexService] 结果不包含图片数据");
                }
                
                results.add(item);
            }
            System.out.println("[IndexService] 返回去重后的结果数: " + results.size());
            return results;
        } catch (IndexNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
