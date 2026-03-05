import { useState, useEffect } from 'react'
import './App.css'

/**
 * 前端根组件：展示 BMAD 系统的基础交互界面。
 */
function App() {
  // 状态管理：跟踪模型加载情况、状态信息及操作日志
  const [isLoaded, setIsLoaded] = useState(false)
  const [statusMessage, setStatusMessage] = useState('等待检查状态...')
  const [handshake, setHandshake] = useState('未连接')
  const [searchQuery, setSearchQuery] = useState('')
  const [isClearing, setIsClearing] = useState(false)
interface SearchResult {
  content: string;
  filename: string;
  score: number;
  max_raw_score: number;
  anchor?: string;
  image_data?: string;
}

  const [isSearching, setIsSearching] = useState(false)
  const [indexedDocs, setIndexedDocs] = useState<string[]>([])
  const [isFetchingDocs, setIsFetchingDocs] = useState(false)
  
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [isDragging, setIsDragging] = useState(false)
  const allowedExtensions = ['pdf', 'docx', 'doc', 'pptx', 'md', 'txt', 'jpg', 'jpeg', 'png']
  const acceptTypes = allowedExtensions.map((ext) => `.${ext}`).join(',')
  const folderInputProps = {
    webkitdirectory: '',
    directory: '',
  } as React.InputHTMLAttributes<HTMLInputElement> & { webkitdirectory?: string; directory?: string }

  // 拟合度计算逻辑：结合原始得分和置信度
  const calculateFitDegreeValue = (score: number, maxRawScore: number) => {
    if (maxRawScore <= 0) return 0;
    const baseConfidence = Math.min(100, (score / maxRawScore) * 100);
    if (maxRawScore < 5.0) {
      return baseConfidence * (maxRawScore / 5.0);
    }
    return baseConfidence;
  };

  const formatFitDegree = (score: number, maxRawScore: number) => {
    return calculateFitDegreeValue(score, maxRawScore).toFixed(2) + '%';
  };

  // 副作用：应用挂载时首次拉取后端状态
  useEffect(() => {
    checkStatus()
    fetchIndexedDocs()
  }, [])

  /**
   * 获取所有已索引的文档列表。
   */
  const fetchIndexedDocs = async () => {
    setIsFetchingDocs(true)
    try {
      const response = await fetch('/api/docs/list')
      if (response.ok) {
        const data = await response.json()
        setIndexedDocs(data)
      }
    } catch (error) {
      console.error('Failed to fetch indexed documents:', error)
    } finally {
      setIsFetchingDocs(false)
    }
  }

  /**
   * 统一上传处理函数，支持单文件、多文件及文件夹。
   */
  const uploadFiles = async (files: FileList | File[]) => {
    if (!files || files.length === 0) return

    const formData = new FormData()
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i])
    }

    setStatusMessage(`正在上传并解析 ${files.length} 个文件...`)
    try {
      const res = await fetch('/api/docs/upload', {
        method: 'POST',
        body: formData,
      })
      const data = await res.json()
      if (data.success) {
        setStatusMessage(`成功处理 ${data.successCount} 个文件！`)
        fetchIndexedDocs() // 刷新文档列表
      } else {
        setStatusMessage(`部分或全部解析失败 (成功: ${data.successCount}, 失败: ${data.failCount}): ${data.error || '未知错误'}`)
      }
    } catch (err) {
      setStatusMessage('上传请求异常')
      console.error(err)
    }
  }

  /**
   * 文件选择处理。
   */
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      uploadFiles(e.target.files)
    }
  }

  /**
   * 拖拽处理逻辑。
   */
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(true)
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      uploadFiles(e.dataTransfer.files)
    }
  }

  /**
   * 搜索知识库。
   */
  const handleSearch = async () => {
    if (!searchQuery.trim()) return
    setIsSearching(true)
    setSearchResults([]) // 清除之前的搜索结果
    try {
      const response = await fetch(`/api/docs/search?q=${encodeURIComponent(searchQuery)}`)
      if (response.ok) {
        const data: SearchResult[] = await response.json()
        
        // 阈值过滤：拟合度低于 10% 的结果不再显示
        const filteredData = data.filter(result => 
          calculateFitDegreeValue(result.score, result.max_raw_score) >= 10
        );

        setSearchResults(filteredData)
        if (filteredData.length === 0) {
          setStatusMessage('未检索到相关内容 (拟合度均低于 10%)')
        } else {
          setStatusMessage(`成功检索到 ${filteredData.length} 条相关内容`)
        }
      } else {
        setStatusMessage('检索请求失败，请检查后端状态')
      }
    } catch (error) {
      console.error('Search failed:', error)
      setStatusMessage('网络错误，请检查后端服务是否启动')
    } finally {
      setIsSearching(false)
    }
  }

  const handleClearIndex = async () => {
    if (!window.confirm('确定要清空所有索引数据吗？此操作不可撤销。')) return
    setIsClearing(true)
    try {
      const response = await fetch('/api/docs/clear', { method: 'DELETE' })
      if (response.ok) {
        alert('索引已成功清空')
        setSearchResults([])
        setSearchQuery('')
        setStatusMessage('索引库已清空')
        fetchIndexedDocs() // 刷新文档列表
      } else {
        alert('清空索引失败')
      }
    } catch (error) {
      console.error('Clear index failed:', error)
      alert('网络错误，请检查后端状态')
    } finally {
      setIsClearing(false)
    }
  }

  /**
   * 接口调用：获取模型状态。
   * 发起 GET 请求到 /api/model/status。
   */
  const checkStatus = async () => {
    try {
      const res = await fetch('/api/model/status')
      const data = await res.json()
      setIsLoaded(data.isLoaded)
      setHandshake(data.handshake)
    } catch (err) {
      console.error('状态检查失败:', err)
      setHandshake('连接后端失败')
    }
  }

  /**
   * 接口调用：触发模型加载。
   * 发起 POST 请求到 /api/model/load。
   */
  const handleLoad = async () => {
    setStatusMessage('正在加载模型，请稍候...')
    try {
      const res = await fetch('/api/model/load', { method: 'POST' })
      const data = await res.json()
      setStatusMessage(data.message)
      checkStatus() // 刷新状态
    } catch (err) {
      console.error(err)
      setStatusMessage('加载请求异常')
    }
  }

  /**
   * 接口调用：触发模型卸载。
   * 发起 POST 请求到 /api/model/unload。
   */
  const handleUnload = async () => {
    setStatusMessage('正在卸载模型...')
    try {
      const res = await fetch('/api/model/unload', { method: 'POST' })
      const data = await res.json()
      setStatusMessage(data.message)
      checkStatus() // 刷新状态
    } catch (err) {
      console.error(err)
      setStatusMessage('卸载请求异常')
    }
  }

  const maxScore = searchResults.length > 0 ? Math.max(...searchResults.map((result) => result.score ?? 0)) : 0
  const formatFitScore = (score: number) => {
    if (maxScore <= 0) return '0.00%'
    const value = Math.max(0, Math.min(100, (score / maxScore) * 100))
    return `${value.toFixed(2)}%`
  }

  return (
    <div className="container">
      <h1>RAG CONSOLE</h1>
      <div className="card status-panel">
        <h3>系统状态</h3>
        <p><strong>JNI 握手:</strong> {handshake}</p>
        <p><strong>加载状态:</strong> {isLoaded ? '✅ 已加载' : '❌ 未加载'}</p>
      </div>

      <div className="card control-panel">
        <h3>模型操作</h3>
        <div className="button-group">
          <button onClick={handleLoad} disabled={isLoaded}>
            加载模型
          </button>
          <button onClick={handleUnload} disabled={!isLoaded}>
            卸载模型
          </button>
          <button onClick={checkStatus}>
            刷新状态
          </button>
        </div>
        <p className="log-message"><strong>操作日志:</strong> {statusMessage}</p>
      </div>

      <div className="card documents-panel">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3>已解析文档 ({indexedDocs.length})</h3>
          <button onClick={fetchIndexedDocs} disabled={isFetchingDocs} style={{ fontSize: '12px', padding: '4px 8px' }}>
            {isFetchingDocs ? '刷新中...' : '刷新列表'}
          </button>
        </div>
        <div className="docs-list" style={{ maxHeight: '150px', overflowY: 'auto', marginTop: '10px', fontSize: '14px' }}>
          {indexedDocs.length === 0 ? (
            <p style={{ color: '#888', textAlign: 'center' }}>暂无已解析文档</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {indexedDocs.map((doc, idx) => (
                <li key={idx} style={{ 
                  padding: '6px 10px', 
                  borderBottom: '1px solid #eee',
                  display: 'flex',
                  alignItems: 'center'
                }}>
                  <span style={{ marginRight: '8px' }}>📄</span>
                  <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{doc}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="card knowledge-panel">
        <h3>RAG Management</h3>
        
        <div 
          className={`upload-zone ${isDragging ? 'drag-active' : ''}`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          <div className="upload-options">
            <label className="upload-button">
              选择文件
              <input type="file" multiple onChange={handleFileChange} accept={acceptTypes} style={{ display: 'none' }} />
            </label>
            
            <label className="upload-button folder-button">
              批量导入文件夹
              <input type="file" {...folderInputProps} multiple onChange={handleFileChange} style={{ display: 'none' }} />
            </label>
          </div>
          <p className="upload-hint">支持拖拽 PDF/DOCX/PPTX/MD/TXT/JPG/JPEG/PNG 文件或文件夹至此区域</p>
        </div>
        
        <div className="search-section" style={{ marginTop: '20px' }}>
          <input 
            type="text" 
            placeholder="输入关键词进行语义搜索..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            className="search-input"
          />
          <button 
            onClick={handleSearch} 
            className="search-button"
            disabled={isSearching}
          >
            {isSearching ? '检索中...' : '语义检索'}
          </button>
          <button 
            onClick={handleClearIndex} 
            className="clear-button" 
            disabled={isClearing}
            style={{ 
              marginLeft: '10px', 
              backgroundColor: '#ff4d4f', 
              color: 'white', 
              border: 'none', 
              padding: '8px 16px', 
              borderRadius: '4px', 
              cursor: isClearing ? 'not-allowed' : 'pointer' 
            }}
          >
            {isClearing ? '正在清空...' : '清空索引'}
          </button>
        </div>

        {searchResults.length > 0 && (
          <div className="search-results" style={{ marginTop: '15px', textAlign: 'left' }}>
            <h4>搜索结果 ({searchResults.length}):</h4>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {searchResults.map((result, index) => (
                <li key={index} className="result-item" style={{ marginBottom: '15px', padding: '10px', border: '1px solid #eee', borderRadius: '4px' }}>
                  <div style={{ fontSize: '0.9em', color: '#666', marginBottom: '5px' }}>
                    <strong>{result.filename}{result.anchor ? ` (${result.anchor})` : ''}</strong> (Score: {result.score.toFixed(4)}, 拟合度: {formatFitDegree(result.score, result.max_raw_score)})
                  </div>
                  {result.image_data && (
                    <div style={{ marginBottom: '10px' }}>
                      <img 
                        src={`data:image/png;base64,${result.image_data}`} 
                        alt="Search Result" 
                        style={{ maxWidth: '100%', maxHeight: '300px', borderRadius: '4px', border: '1px solid #ddd' }} 
                      />
                    </div>
                  )}
                  <div style={{ whiteSpace: 'pre-wrap' }}>{result.content}</div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      <div className="footer">
        <p>Local Knowledge RAG System - Powered by llama.cpp JNI</p>
      </div>
    </div>
  )
}

export default App
