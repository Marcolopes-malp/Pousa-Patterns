# Estágio de Compilação com Maven e OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .

# C2: Otimização de cache Docker baixando dependências antes do código-fonte
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Estágio de Execução com Apache Tomcat Oficial 9.0.98 alinhado ao pom.xml
FROM tomcat:9.0.98-jdk17-temurin
LABEL maintainer="Marco Antonio Lopes Pedro"

# C2: Configuração de usuário não-root para segurança e diretório de dados
RUN groupadd -r appgroup && useradd -r -g appgroup -d /usr/local/tomcat appuser && \
    mkdir -p /app/data && chown -R appuser:appgroup /usr/local/tomcat /app/data

# Remover aplicações padrão do Tomcat e implantar como ROOT
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/pousada-reservas.war /usr/local/tomcat/webapps/ROOT.war
RUN chown appuser:appgroup /usr/local/tomcat/webapps/ROOT.war

# Volume para persistência dos dados do banco H2
VOLUME /app/data
ENV DB_PATH=/app/data/pousada_db

USER appuser

EXPOSE 8080
CMD ["catalina.sh", "run"]
