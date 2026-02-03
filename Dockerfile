FROM eclipse-temurin:17-jdk

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy Maven configuration and source files
COPY pom.xml .
COPY src ./src

# Download dependencies and compile with Maven
RUN mvn clean compile

# Copy dependencies to target directory
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

# Expose the port (Render will set PORT env variable)
EXPOSE 8080

# Run the application with classpath including dependencies
CMD ["java", "-cp", "target/classes:target/dependency/*", "Converter"]