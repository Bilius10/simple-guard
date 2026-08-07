FROM node:24-alpine AS build
WORKDIR /workspace

COPY package*.json ./
RUN npm install --no-audit --no-fund

COPY angular.json tsconfig*.json ./
COPY public ./public
COPY src ./src
RUN npm run test && npm run build

FROM nginxinc/nginx-unprivileged:1.29-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/web-admin/browser /usr/share/nginx/html

EXPOSE 8080
