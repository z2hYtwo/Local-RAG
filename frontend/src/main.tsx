import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'

// 以并发渲染模式创建根节点，绑定到 index.html 中的 #root
ReactDOM.createRoot(document.getElementById('root')!).render(
  // 严格模式仅在开发阶段启用额外检查，不影响生产构建
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
