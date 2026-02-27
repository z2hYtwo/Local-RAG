import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

/**
 * Vite 项目配置文件。
 * 
 * Vite 是一款面向现代浏览器的前端构建工具，其核心优势在于：
 * 1. 极速的开发模式 (HMR)：基于 ES Modules 的实时更新，无需等待整个应用打包。
 * 2. 预构建：使用 esbuild 对三方库进行预处理，极大提升加载效率。
 */
export default defineConfig({
  /**
   * 插件配置：
   * 使用 @vitejs/plugin-react-swc 插件。
   * SWC (Speedy Web Compiler) 是一个基于 Rust 编写的高性能 JavaScript/TypeScript 编译器。
   * 它替代了传统的 Babel，使得 React 项目的编译速度提升了 10~20 倍，特别是在大型项目中效果显著。
   */
  plugins: [react()],
  /**
   * 开发服务器配置：
   * 设置代理 (Proxy) 将前端 API 请求转发到 Spring Boot 后端。
   * 解决了本地开发过程中的跨域 (CORS) 问题。
   */
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
