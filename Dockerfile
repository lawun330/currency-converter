FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the Java source file
COPY Converter.java .

# Compile the Java file
RUN javac Converter.java

# Expose the port (Render will set PORT env variable)
EXPOSE 8080

# Run the application
CMD ["java", "Converter"]