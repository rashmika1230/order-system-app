# Order System

A Spring Boot microservices project with a production-style DevOps pipeline:
GitHub Actions, Terraform, k3s on AWS, ArgoCD and Prometheus/Grafana.

## Services

- **api-gateway**: single entry point, routes requests
- **product-service**: manages products and stock
- **order-service**: places orders, calls product-service

## Status

Work in progress.
