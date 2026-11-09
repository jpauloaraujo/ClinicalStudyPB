# T67: imagem da aplicação web para o ambiente completo do docker-compose.yml.
# Estágio 1 gera o build de produção do Vite; estágio 2 serve os arquivos estáticos com nginx.

FROM node:22-alpine AS build
WORKDIR /src

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
# A URL é lida pelo navegador do usuário, não pelo container: por isso o padrão é localhost:8081
# (porta do backend publicada no host). O Vite embute o valor no build.
ARG VITE_API_URL=http://localhost:8081
ENV VITE_API_URL=$VITE_API_URL \
    VITE_USE_MOCKS=false
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --retries=6 \
    CMD wget -q -O /dev/null http://127.0.0.1/login || exit 1
