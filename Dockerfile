# Use official OpenJDK runtime image
FROM openjdk:25-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and POM files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give execute permissions to Maven wrapper
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:resolve -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Expose port
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "target/hris-0.0.1-SNAPSHOT.jar"]