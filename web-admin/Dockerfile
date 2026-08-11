FROM node:24-alpine AS build
WORKDIR /workspace
ARG SIMPLEGUARD_WEB_OIDC_ISSUER=https://idp.localhost/realms/simpleguard

COPY package*.json ./
RUN npm install --no-audit --no-fund

COPY angular.json tsconfig*.json ./
COPY public ./public
COPY src ./src
RUN node -e "const fs = require('fs'); const path = 'src/app/auth/auth.config.ts'; const issuer = process.env.SIMPLEGUARD_WEB_OIDC_ISSUER; const source = fs.readFileSync(path, 'utf8'); fs.writeFileSync(path, source.replace('https://idp.localhost/realms/simpleguard', issuer));"
RUN npm run test && npm run build

FROM nginxinc/nginx-unprivileged:1.29-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/web-admin/browser /usr/share/nginx/html

EXPOSE 8080
