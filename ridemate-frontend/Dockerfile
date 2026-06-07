# ─────────────────────────────────────────────────────
# Stage 1: Build the Angular app
# ─────────────────────────────────────────────────────
FROM node:18-alpine AS builder

WORKDIR /app

# Copy package files first (better Docker layer caching)
COPY package.json package-lock.json ./
RUN npm ci --silent

# Copy source and build
COPY . .
RUN npm run build -- --configuration=production

# ─────────────────────────────────────────────────────
# Stage 2: Serve with Nginx (lightweight, production-ready)
# ─────────────────────────────────────────────────────
FROM nginx:alpine

# Remove default nginx page
RUN rm -rf /usr/share/nginx/html/*

# Copy Angular build output
COPY --from=builder /app/dist/ridemate-frontend/browser /usr/share/nginx/html

# Copy custom nginx config (handles Angular routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
