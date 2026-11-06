import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue2";
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: process.env.API_TARGET || "http://127.0.0.1:8080",
        changeOrigin: true,
      },
      "/actuator": {
        target: process.env.API_TARGET || "http://127.0.0.1:8080",
      },
    },
  },
});
