# Build Stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

RUN apt-get update && \
	DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends git curl && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/*

WORKDIR /app

ARG GITHUB_TOKEN

# Fetch the parent POM from GitHub and install it
RUN curl -fsSL https://raw.githubusercontent.com/Deathrow002/Core-Banking/master/pom.xml -o /app/pom.xml
RUN mvn clean install -N

# Clone the Loan service from GitHub
RUN git clone --branch main --single-branch https://${GITHUB_TOKEN}@github.com/Deathrow002/Core-Banking-Loan.git loan

# Build the Loan service
RUN mkdir -p loan/src/main/avro loan/src/test/avro
RUN mvn clean package -DskipTests -f loan/pom.xml

# Runtime Stage
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
	DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends wget curl && \
	apt-get upgrade -y && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/loan/target/*.jar loan-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "loan-service.jar"]
