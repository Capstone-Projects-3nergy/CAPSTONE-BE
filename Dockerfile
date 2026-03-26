# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# build
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# run as non-root
RUN useradd -m -u 1001 spring
USER spring

COPY --from=build /app/target/*.jar /app/app.jar

ENV TZ=Asia/Bangkok \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]