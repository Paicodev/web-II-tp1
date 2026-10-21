# Etapa 1: Compilación del código con Maven y Java 25
FROM maven:3.9.9-eclipse-temurin-25-noble AS build
WORKDIR /app

# Copiamos descriptores de dependencias para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos sin ejecutar tests en el empaquetado final
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen liviana de ejecución (JRE)
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Creamos un usuario no privilegiado por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copiamos únicamente el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
