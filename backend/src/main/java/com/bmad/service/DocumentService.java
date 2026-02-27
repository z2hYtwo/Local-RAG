package com.bmad.service;

import com.bmad.model.DocumentSegment;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档解析服务。
 * 
 * 职责：
 * 1. 支持多种格式的文档解析（PDF, DOCX, PPTX, Markdown, TXT）。
 * 2. 将文档内容提取为 DocumentSegment 列表，包含元数据（如页码、幻灯片编号）。
 */
@Service
public class DocumentService {

    /**
     * 解析上传的文件。
     * 
     * @param file 上传的多部分文件
     * @return 解析后的文档片段列表
     * @throws IOException 如果解析过程中出现错误
     */
    public List<DocumentSegment> parseDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) return Collections.emptyList();

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        try (InputStream is = file.getInputStream()) {
            switch (extension) {
                case "pdf":
                    return parsePdf(is);
                case "docx":
                    return parseDocx(is);
                case "pptx":
                    return parsePptx(is);
                case "md":
                case "txt":
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    DocumentSegment seg = new DocumentSegment(content);
                    seg.addMetadata("source_type", extension);
                    return Collections.singletonList(seg);
                case "jpg":
                case "jpeg":
                case "png":
                    return parseStandaloneImage(file, extension);
                default:
                    throw new IllegalArgumentException("不支持的文件格式: " + extension);
            }
        }
    }

    /**
     * 使用 PDFBox 3.0 解析 PDF 文件。
     */
    private List<DocumentSegment> parsePdf(InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        List<DocumentSegment> segments = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int numPages = document.getNumberOfPages();
            
            for (int i = 1; i <= numPages; i++) {
                // 1. 提取文本
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document).trim();
                
                if (!text.isEmpty()) {
                    DocumentSegment seg = new DocumentSegment(text);
                    seg.addMetadata("page_number", i);
                    seg.addMetadata("source_type", "pdf");
                    segments.add(seg);
                }

                // 2. 提取页面图片 (实验性)
                PDPage page = document.getPage(i - 1);
                PDResources resources = page.getResources();
                for (COSName name : resources.getXObjectNames()) {
                    if (resources.isImageXObject(name)) {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                        BufferedImage bufferedImage = image.getImage();
                        
                        String base64 = encodeImageToBase64(bufferedImage, "png");
                        // 为图片创建一个描述片段，方便检索
                        String description = "PDF Image on Page " + i + " in " + name.getName();
                        DocumentSegment imgSeg = new DocumentSegment(description, base64);
                        imgSeg.addMetadata("page_number", i);
                        imgSeg.addMetadata("source_type", "pdf_image");
                        segments.add(imgSeg);
                    }
                }
            }
        }
        return segments;
    }

    private List<DocumentSegment> parseStandaloneImage(MultipartFile file, String extension) throws IOException {
        try (InputStream is = file.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(is);
            if (bufferedImage == null) {
                throw new IOException("无法读取图片内容: " + file.getOriginalFilename());
            }
            String base64 = encodeImageToBase64(bufferedImage, extension);
             // 对于独立图片，使用文件名作为描述
             String filename = file.getOriginalFilename();
             String baseName = filename != null ? filename.substring(0, filename.lastIndexOf('.')) : "image";
             DocumentSegment seg = new DocumentSegment("Image: " + baseName + " (" + filename + ")", base64);
             seg.addMetadata("source_type", "image");
             seg.addMetadata("filename", filename);
             return Collections.singletonList(seg);
        }
    }

    private String encodeImageToBase64(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * 使用 Apache POI 解析 DOCX 文件。
     */
    private List<DocumentSegment> parseDocx(InputStream is) throws IOException {
        try (XWPFDocument document = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            List<DocumentSegment> segments = new ArrayList<>();
            String currentSectionHeader = null;

            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph paragraph = paragraphs.get(i);
                String text = paragraph.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }

                String style = paragraph.getStyle();
                if (style != null) {
                    String normalized = style.toLowerCase();
                    if (normalized.startsWith("heading") || normalized.contains("heading")) {
                        currentSectionHeader = text.trim();
                    }
                }

                DocumentSegment seg = new DocumentSegment(text.trim());
                seg.addMetadata("source_type", "docx");
                seg.addMetadata("paragraph_index", i + 1);
                if (currentSectionHeader != null) {
                    seg.addMetadata("section_header", currentSectionHeader);
                }
                segments.add(seg);
            }

            if (segments.isEmpty()) {
                String fallback = paragraphs.stream()
                        .map(XWPFParagraph::getText)
                        .collect(Collectors.joining("\n"))
                        .trim();
                if (fallback.isEmpty()) {
                    return Collections.emptyList();
                }
                DocumentSegment seg = new DocumentSegment(fallback);
                seg.addMetadata("source_type", "docx");
                return Collections.singletonList(seg);
            }

            return segments;
        }
    }

    /**
     * 使用 Apache POI 解析 PPTX 文件。
     */
    private List<DocumentSegment> parsePptx(InputStream is) throws IOException {
        List<DocumentSegment> segments = new ArrayList<>();
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            List<XSLFSlide> slides = ppt.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                XSLFSlide slide = slides.get(i);
                StringBuilder text = new StringBuilder();
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String shapeText = textShape.getText();
                        if (shapeText != null && !shapeText.isBlank()) {
                            text.append(shapeText).append('\n');
                        }
                    }
                }
                
                String content = text.toString().trim();
                if (!content.isEmpty()) {
                    DocumentSegment seg = new DocumentSegment(content);
                    seg.addMetadata("slide_number", i + 1);
                    segments.add(seg);
                }
            }
        }
        return segments;
    }
}
