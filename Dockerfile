# Etapa 1: Compilación con OpenJDK 25 usando el wrapper del proyecto
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos descriptores y el wrapper de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Corregimos saltos de línea de Windows (CRLF a LF) y damos permisos de ejecución
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargamos dependencias para aprovechar la caché
RUN ./mvnw dependency:go-offline -B

# Copiamos el código fuente y empaquetamos el jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen liviana de ejecución (JRE 25)
FROM eclipse-temurin:25-jre
WORKDIR /app

# Usuario no privilegiado para mayor seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
