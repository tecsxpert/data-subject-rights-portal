# ── Stage 1: Build React app ──────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .

# VITE_API_URL is injected at build time from docker-compose args
ARG VITE_API_URL=http://localhost:8080/api/v1
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# ── Stage 2: Serve with Nginx ─────────────────────────────────
FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
