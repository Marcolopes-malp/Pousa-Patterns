# Estágio de Compilação com Maven e OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio de Execução com Apache Tomcat Oficial
FROM tomcat:9.0-jdk17-temurin
LABEL maintainer="Marco Antonio Lopes Pedro"
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/pousada-reservas.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
