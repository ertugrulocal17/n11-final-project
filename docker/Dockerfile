# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY libs ./libs
COPY services ./services
COPY gateway ./gateway
# mvnw
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine AS auth-service
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/services/auth-service/target/auth-service-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine AS product-service
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/services/product-service/target/product-service-*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine AS cart-service
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/services/cart-service/target/cart-service-*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine AS order-service
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/services/order-service/target/order-service-*.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine AS payment-service
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/services/payment-service/target/payment-service-*.jar /app/app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine AS api-gateway
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/gateway/api-gateway/target/api-gateway-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
