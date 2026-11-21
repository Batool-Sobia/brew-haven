# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY src ./src
RUN mkdir -p bin && javac -d bin src/*.java

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/bin ./bin
COPY public ./public
RUN mkdir -p data/sales

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "bin", "src.Main"]
