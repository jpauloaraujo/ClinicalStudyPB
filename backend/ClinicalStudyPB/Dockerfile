# T67: imagem do backend (Spring Boot, Java 21) para o ambiente completo do docker-compose.yml.
# Estágio 1 compila o jar sem rodar os testes (eles rodam no CI); estágio 2 só executa.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# O Maven Central pode recusar os downloads dos runners do CI com 429 (Too Many Requests), que é um limite temporário
# do serviço e não um erro do projeto. Por isso cada passo que baixa dependências tenta até 5 vezes, esperando mais
# a cada falha (20 s, 40 s, 60 s e 80 s), e só então falha de verdade.

# Dependências primeiro: a camada fica em cache enquanto o pom.xml não mudar
COPY pom.xml .
RUN for tentativa in 1 2 3 4 5; do \
        mvn -B -q dependency:go-offline && exit 0; \
        echo "Maven falhou na tentativa $tentativa de 5 (limite do Maven Central?); aguardando para tentar de novo"; \
        [ "$tentativa" -lt 5 ] && sleep $((tentativa * 20)); \
    done; \
    exit 1

COPY src ./src
RUN for tentativa in 1 2 3 4 5; do \
        if mvn -B -q -DskipTests package; then \
            find target -maxdepth 1 -name '*.jar' ! -name '*.original' -exec cp {} /app.jar \; ; \
            exit 0; \
        fi; \
        echo "Maven falhou na tentativa $tentativa de 5 (limite do Maven Central?); aguardando para tentar de novo"; \
        [ "$tentativa" -lt 5 ] && sleep $((tentativa * 20)); \
    done; \
    exit 1

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Roda sem root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app.jar app.jar
USER app

EXPOSE 8081
# O wget do BusyBox já vem na imagem Alpine; /v3/api-docs é público (Swagger)
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
    CMD wget -q -O /dev/null http://127.0.0.1:8081/v3/api-docs || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
