# ============================================================
# AquaTrack Frontend — Dockerfile
# ============================================================
# Stage 1: Build the Vite React app
# Stage 2: Serve via Nginx (production) or run Vite dev server
# ============================================================

# ── Stage 1: Build ───────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first (cache dependency install layer)
COPY package.json package-lock.json* ./

RUN npm ci --prefer-offline

# Copy source
COPY . .

# Build args for environment variables baked into the bundle
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ── Stage 2: Serve with Nginx ─────────────────────────────────
FROM nginx:alpine AS production

# Copy built assets
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx config for SPA routing (all paths return index.html)
RUN echo 'server { \
    listen 80; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { try_files $uri $uri/ /index.html; } \
    gzip on; gzip_types text/plain text/css application/json application/javascript; \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
