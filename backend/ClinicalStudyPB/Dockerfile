# T67: imagem do backend (Spring Boot, Java 21) para o ambiente completo do docker-compose.yml.
# Estágio 1 compila o jar sem rodar os testes (eles rodam no CI); estágio 2 só executa.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Dependências primeiro: a camada fica em cache enquanto o pom.xml não mudar
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && find target -maxdepth 1 -name '*.jar' ! -name '*.original' -exec cp {} /app.jar \;

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
