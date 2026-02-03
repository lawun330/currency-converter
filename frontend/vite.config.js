import path from 'path'
import { fileURLToPath } from 'url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
// load .env from project root (one file for MongoDB, Java, and Vite)
const rootEnvDir = path.resolve(__dirname, '..')

export default defineConfig({
  plugins: [react()],
  envDir: rootEnvDir,
})
