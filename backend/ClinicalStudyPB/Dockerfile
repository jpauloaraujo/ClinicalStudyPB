# T67: imagem do backend (Spring Boot, Java 21) para o ambiente completo do docker-compose.yml.
# Estágio 1 compila o jar sem rodar os testes (eles rodam no CI); estágio 2 só executa.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# O Maven Central recusa os downloads dos runners do CI com 429 (Too Many Requests) por períodos longos, de vários
# minutos, e repetir contra o mesmo servidor não resolve (D-S3-14). Por isso cada passo que baixa dependências segue
# esta ordem: 2 tentativas no Maven Central e, se ele continuar recusando, até 3 tentativas em um espelho dele (Google
# Cloud Storage). Só depois de todas as tentativas o build falha. O espelho só entra em ação como reserva.
RUN printf '%s\n' \
      '<settings>' \
      '  <mirrors>' \
      '    <mirror>' \
      '      <id>espelho-central</id>' \
      '      <mirrorOf>central</mirrorOf>' \
      '      <url>https://maven-central.storage-download.googleapis.com/maven2/</url>' \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' > /tmp/settings-espelho.xml

# Dependências primeiro: a camada fica em cache enquanto o pom.xml não mudar
COPY pom.xml .
RUN for tentativa in 1 2; do \
        mvn -B -q dependency:go-offline && exit 0; \
        echo "Maven Central falhou na tentativa $tentativa de 2 (limite de requisições?)"; \
        [ "$tentativa" -lt 2 ] && sleep $((tentativa * 15)); \
    done; \
    echo "Usando o espelho do Maven Central"; \
    for tentativa in 1 2 3; do \
        mvn -B -q -s /tmp/settings-espelho.xml dependency:go-offline && exit 0; \
        echo "O espelho falhou na tentativa $tentativa de 3"; \
        [ "$tentativa" -lt 3 ] && sleep $((tentativa * 15)); \
    done; \
    exit 1

COPY src ./src
RUN for tentativa in 1 2; do \
        if mvn -B -q -DskipTests package; then \
            find target -maxdepth 1 -name '*.jar' ! -name '*.original' -exec cp {} /app.jar \; ; \
            exit 0; \
        fi; \
        echo "Maven Central falhou na tentativa $tentativa de 2 (limite de requisições?)"; \
        [ "$tentativa" -lt 2 ] && sleep $((tentativa * 15)); \
    done; \
    echo "Usando o espelho do Maven Central"; \
    for tentativa in 1 2 3; do \
        if mvn -B -q -s /tmp/settings-espelho.xml -DskipTests package; then \
            find target -maxdepth 1 -name '*.jar' ! -name '*.original' -exec cp {} /app.jar \; ; \
            exit 0; \
        fi; \
        echo "O espelho falhou na tentativa $tentativa de 3"; \
        [ "$tentativa" -lt 3 ] && sleep $((tentativa * 15)); \
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
