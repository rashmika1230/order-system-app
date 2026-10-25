# Order System

A Spring Boot microservices project with a production-style DevOps pipeline:
GitHub Actions, Terraform, k3s on AWS, ArgoCD and Prometheus/Grafana.

## Services

- **api-gateway**: single entry point, routes requests
- **product-service**: manages products and stock
- **order-service**: places orders, calls product-service

## Status

Work in progress.

## Running locally

Prerequisites: Java 17, Docker Desktop.

1. Copy `.env.example` to `.env` and set a root password
2. Start MySQL: `docker compose up -d`
3. Start the services (each in its own terminal):
    - `cd product-service && ./mvnw spring-boot:run`  (port 8081)
    - `cd order-service && ./mvnw spring-boot:run`    (port 8082)
    - `cd api-gateway && ./mvnw spring-boot:run`      (port 8080)
4. Call the API through the gateway at `http://localhost:8080`.
   Example requests are in each service's `http/` folder.

Only `/api/products`, `/api/products/{id}`, `/api/orders` and `/api/orders/{id}` are public.
The stock reservation endpoint is internal (order-service → product-service only).
