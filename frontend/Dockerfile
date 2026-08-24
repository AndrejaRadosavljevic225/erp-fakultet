# syntax=docker/dockerfile:1
# --- Build (Node 22) ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Prazan VITE_API_URL = isti origin; nginx proksira /hr i /schedule na gateway
ENV VITE_API_URL=""
RUN npm run build

# --- Run (nginx) ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
