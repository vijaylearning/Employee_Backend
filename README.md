# End-to-End Spring Boot + PostgreSQL CI/CD with Kubernetes  
**Authors:** Vijeyender Ravi  

---

## 📌 1. Overview
This document explains how to:
- Build a **Spring Boot** application with **PostgreSQL**
- Containerize it using **Docker**
- Automate builds using **Jenkins**
- Deploy to **Kubernetes** with **Persistent Volumes** for database storage
- Expose the application using **NGINX Ingress**

---

## 🛠️ 2. Spring Boot Application
- **REST API backend** using Spring Boot  
- Uses **JPA + PostgreSQL** for database operations

---

## 🐳 3. Docker Image

### Dockerfile (line-by-line explained)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine        # Use lightweight JDK 17 base image
VOLUME /tmp                               # Mount /tmp to support Spring Boot temp files
COPY target/app.jar app.jar               # Copy built JAR into container
ENTRYPOINT ["java","-jar","/app.jar"]     # Start the Spring Boot app
