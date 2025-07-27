✅ Spring Boot + PostgreSQL setup
✅ Docker image creation
✅ Jenkins CI/CD pipeline
✅ Kubernetes deployment with YAML explanation (line-by-line) 
✅ Manual steps to perform every time an image is deployed

📄 Document: End-to-End Spring Boot + PostgreSQL CI/CD with Kubernetes
✍️ Author: Karthick Pitchaimari & Vijeyender Ravi
📌 1. Overview
This document explains how to:

Build a Spring Boot application with PostgreSQL
Containerize it using Docker
Automate builds using Jenkins
Deploy to Kubernetes with Persistent Volumes for database storage
Expose the application using NGINX Ingress
🛠️ 2. Spring Boot Application
REST API backend using Spring Boot.
Uses JPA + PostgreSQL for database operations.
🐳 3. Docker Image
Dockerfile (explained line-by-line)
FROM eclipse-temurin:17-jdk-alpine        # Use lightweight JDK 17 base image
VOLUME /tmp                               # Mount /tmp to support Spring Boot temp files
COPY target/app.jar app.jar               # Copy built JAR into container
ENTRYPOINT ["java","-jar","/app.jar"]     # Start the Spring Boot app
🔹 Manual Step:

Run mvn clean package -DskipTests to build the JAR.

Build Docker image:

docker build -t <dockerhub-username>/employee-backend:<version> .
docker push <dockerhub-username>/employee-backend:<version>
🔧 4. Jenkins Pipeline
Jenkinsfile (explained)
Checkout Code – Pulls latest code from Git.
Build & Test – Runs Maven to build.
Docker Build & Push – Creates image and pushes to Docker Hub.
Deploy to Kubernetes – Applies Kubernetes YAMLs.
🔹 Manual Step:

Update image version in deployment YAML or use Jenkins to replace tag dynamically.
🗄️ 5. PostgreSQL Setup
postgresdb-PersistentVolumeClaim.yml
Creates a PVC so DB data persists after pod restarts.
postgresdb-deployment.yml
Deploys PostgreSQL.
Uses environment variables for DB name, user, password.
Mounts PVC to /var/lib/postgresql/data.
postgresdb-service.yml
Exposes PostgreSQL internally at postgres-service:5432.
🚀 6. Spring Boot Deployment
Deployment.yml
Deploys the Spring Boot application.

Important lines:

image: <dockerhub-username>/employee-backend:<version>  # Must update each deploy
env:
  - name: SPRING_DATASOURCE_URL
    value: jdbc:postgresql://postgres-service:5432/employee_db
Connects app to PostgreSQL using internal service DNS.

service.yml
Creates a ClusterIP service employee-backend-service.
Maps port 80 → pod's port 8080.
🌐 7. Ingress
ingress.yml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: employee-backend-ingress
spec:
  ingressClassName: nginx                # Use NGINX controller
  rules:
    - host: employee.local               # Access via http://employee.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: employee-backend-service
                port:
                  number: 80
🔹 Manual Step: Add to /etc/hosts:

127.0.0.1 employee.local
📌 8. Deployment Steps
1️⃣ Build & Push Image
mvn clean package -DskipTests
docker build -t <dockerhub-username>/employee-backend:<version> .
docker push <dockerhub-username>/employee-backend:<version>
2️⃣ Apply Kubernetes Resources
kubectl apply -f postgresdb-PersistentVolumeClaim.yml
kubectl apply -f postgresdb-deployment.yml
kubectl apply -f postgresdb-service.yml

kubectl apply -f Deployment.yml
kubectl apply -f service.yml
kubectl apply -f ingress.yml
3️⃣ Verify
kubectl get pods
kubectl get svc
kubectl get ingress
4️⃣ Access Application
👉 http://employee.local/swagger-ui/index.html

🛑 9. Manual Actions Required on Every Deployment
✅ Build JAR using Maven ✅ Update Docker image tag/version ✅ Push image to Docker Hub ✅ Update Deployment YAML with new tag (or use latest) ✅ Re-apply Deployment YAML:

kubectl apply -f Deployment.yml
✅ Or rollout restart:

kubectl rollout restart deployment employee-backend
📌 10. Debugging
Check app logs:

kubectl logs <employee-backend-pod>
Check ingress controller logs:

kubectl logs -n ingress-nginx <controller-pod>
Verify service connectivity:

kubectl exec -it <any-pod> -- curl http://employee-backend-service:80/swagger-ui/index.html
👉 Next: Do you want me to generate this document as a proper formatted PDF/Markdown file with all YAMLs included and diagrams (flow of Jenkins → Docker → Kubernetes → Service → Pod)? Or keep it as a plain text document?
