FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml pom.xml
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

RUN useradd --system --uid 10001 --create-home appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:+UseContainerSupport -XX:MaxRAMPercentage=75} -jar app.jar"]
