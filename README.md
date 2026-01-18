 **detailed technical document** authored by *Vijeyender Ravi* covering:

✅ Spring Boot + PostgreSQL setup
✅ Docker image creation
✅ Jenkins CI/CD pipeline
✅ Kubernetes deployment with YAML explanation (line-by-line)
✅ Manual steps to perform every time an image is deployed

---

## 📄 **Document: End-to-End Spring Boot + PostgreSQL CI/CD with Kubernetes**

### ✍️ Author: *Vijeyender Ravi & Karthick Pitchaimari *

---

### 📌 1. **Overview**

This document explains how to:

* Build a Spring Boot application with PostgreSQL
* Containerize it using Docker
* Automate builds using Jenkins
* Deploy to Kubernetes with Persistent Volumes for database storage
* Expose the application using NGINX Ingress

---

## 🛠️ 2. **Spring Boot Application**

* REST API backend using Spring Boot.
* Uses JPA + PostgreSQL for database operations.

---

## 🐳 3. **Docker Image**

### `Dockerfile` (explained line-by-line)

```dockerfile
FROM eclipse-temurin:17-jdk-alpine        # Use lightweight JDK 17 base image
VOLUME /tmp                               # Mount /tmp to support Spring Boot temp files
COPY target/app.jar app.jar               # Copy built JAR into container
ENTRYPOINT ["java","-jar","/app.jar"]     # Start the Spring Boot app
```

🔹 **Manual Step:**

* Run `mvn clean package -DskipTests` to build the JAR.
* Build Docker image:

  ```bash
  docker build -t <dockerhub-username>/employee-backend:<version> .
  docker push <dockerhub-username>/employee-backend:<version>
  ```

---

## 🔧 4. **Jenkins Pipeline**

### `Jenkinsfile` (explained)

* **Checkout Code** – Pulls latest code from Git.
* **Build & Test** – Runs Maven to build.
* **Docker Build & Push** – Creates image and pushes to Docker Hub.
* **Deploy to Kubernetes** – Applies Kubernetes YAMLs.

🔹 **Manual Step:**

* Update image version in deployment YAML or use Jenkins to replace tag dynamically.

---

## 🗄️ 5. **PostgreSQL Setup**

### `postgresdb-PersistentVolumeClaim.yml`

* Creates a **PVC** so DB data persists after pod restarts.

### `postgresdb-deployment.yml`

* Deploys PostgreSQL.
* Uses environment variables for DB name, user, password.
* Mounts PVC to `/var/lib/postgresql/data`.

### `postgresdb-service.yml`

* Exposes PostgreSQL internally at `postgres-service:5432`.

---

## 🚀 6. **Spring Boot Deployment**

### `Deployment.yml`

* Deploys the Spring Boot application.
* **Important lines:**

  ```yaml
  image: <dockerhub-username>/employee-backend:<version>  # Must update each deploy
  env:
    - name: SPRING_DATASOURCE_URL
      value: jdbc:postgresql://postgres-service:5432/employee_db
  ```
* Connects app to PostgreSQL using internal service DNS.

### `service.yml`

* Creates a ClusterIP service `employee-backend-service`.
* Maps port 80 → pod's port 8080.

---

## 🌐 7. **Ingress**

### `ingress.yml`

```yaml
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
```

🔹 **Manual Step:** Add to `/etc/hosts`:

```
127.0.0.1 employee.local
```

---

## 📌 8. **Deployment Steps**

### 1️⃣ Build & Push Image

```bash
mvn clean package -DskipTests
docker build -t <dockerhub-username>/employee-backend:<version> .
docker push <dockerhub-username>/employee-backend:<version>
```

### 2️⃣ Apply Kubernetes Resources

```bash
kubectl apply -f postgresdb-PersistentVolumeClaim.yml
kubectl apply -f postgresdb-deployment.yml
kubectl apply -f postgresdb-service.yml

kubectl apply -f Deployment.yml
kubectl apply -f service.yml
kubectl apply -f ingress.yml
```

### 3️⃣ Verify

```bash
kubectl get pods
kubectl get svc
kubectl get ingress
```

### 4️⃣ Access Application

👉 `http://employee.local/swagger-ui/index.html`

---

## 🛑 9. **Manual Actions Required on Every Deployment**

✅ Build JAR using Maven
✅ Update Docker image tag/version
✅ Push image to Docker Hub
✅ Update Deployment YAML with new tag (or use `latest`)
✅ Re-apply Deployment YAML:

```bash
kubectl apply -f Deployment.yml
```

✅ Or rollout restart:

```bash
kubectl rollout restart deployment employee-backend
```

---

## 📌 10. **Debugging**

* Check app logs:

  ```bash
  kubectl logs <employee-backend-pod>
  ```
* Check ingress controller logs:

  ```bash
  kubectl logs -n ingress-nginx <controller-pod>
  ```
* Verify service connectivity:

  ```bash
  kubectl exec -it <any-pod> -- curl http://employee-backend-service:80/swagger-ui/index.html
  ```

---

When you have **multiple services** (for example: `employee-backend`, `customer-backend`, `order-backend`), you configure **one ingress resource with multiple rules or multiple paths**.

---

## ✅ Option 1: Use Different Hostnames for Each Service

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: multi-service-ingress
spec:
  ingressClassName: nginx
  rules:
    - host: employee.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: employee-backend-service
                port:
                  number: 80
    - host: customer.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: customer-backend-service
                port:
                  number: 80
    - host: order.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: order-backend-service
                port:
                  number: 80
```

👉 Access:

* `http://employee.local`
* `http://customer.local`
* `http://order.local`

📌 **Manual:** Add all to `/etc/hosts`:

```
127.0.0.1 employee.local customer.local order.local
```

---

## ✅ Option 2: Use One Hostname with Different Paths

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: multi-service-ingress
spec:
  ingressClassName: nginx
  rules:
    - host: api.local
      http:
        paths:
          - path: /employee
            pathType: Prefix
            backend:
              service:
                name: employee-backend-service
                port:
                  number: 80
          - path: /customer
            pathType: Prefix
            backend:
              service:
                name: customer-backend-service
                port:
                  number: 80
          - path: /order
            pathType: Prefix
            backend:
              service:
                name: order-backend-service
                port:
                  number: 80
```

👉 Access:

* `http://api.local/employee`
* `http://api.local/customer`
* `http://api.local/order`

📌 **Manual:** Add `api.local` to `/etc/hosts`.

---

## ✅ Option 3: Use Regex & Rewrite for Clean URLs

If you want `/` to go to employee, `/customer` to customer, etc.:

```yaml
metadata:
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
    - host: api.local
      http:
        paths:
          - path: /employee(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: employee-backend-service
                port:
                  number: 80
          - path: /customer(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: customer-backend-service
                port:
                  number: 80
```

👉 `/employee/swagger-ui/index.html` will go to employee backend properly.

---

## 📌 When to use which?

* **Different domains (microservices)** → Option 1 (Host-based routing)
* **Single domain API Gateway** → Option 2 or 3 (Path-based routing)

---

---

# **Configuring a DNS Name for a Public IP in Azure**

## **Prerequisites**

* You already have:

  * An Azure subscription.
  * A **Public IP Address** resource in Azure (used by your application service/load balancer/ingress).
* Access to the **Azure Portal** or **Azure CLI**.

---

## **Step 1: Find Your Public IP Resource**

1. Go to **Azure Portal** → **All Resources**.
2. Search for your **Public IP Address** resource (the one used by your application).
3. Click on it to open its settings.

---

## **Step 2: Configure DNS Name**

1. In the **Public IP Address** blade:

   * Under **Settings**, select **Configuration**.
2. In the **DNS Name Label** field:

   * Enter a unique name, e.g., `myapp-demo`.
3. Select **Save**.

> **Important:** The DNS name label must be unique within the Azure region.
> For example:
> If your region is East US and your label is `myapp-demo`, your full DNS will be:

```
myapp-demo.eastus.cloudapp.azure.com
```

---

## **Step 3: Verify the DNS**

1. Open a browser and visit:

   ```
   http://<your-dns-name>.<region>.cloudapp.azure.com
   ```
2. Example:

   ```
   http://myapp-demo.eastus.cloudapp.azure.com
   ```
3. You should now see your application loading.

---

## **Optional: Map to a Custom Domain (e.g., `app.mydomain.com`)**

If you have your own domain, you can map it to this Azure DNS:

1. Go to your **Domain Registrar** (GoDaddy, Namecheap, etc.).
2. Create a **CNAME record**:

   * **Host:** `app` (or whatever subdomain you want)
   * **Value:** `<your-dns-label>.<region>.cloudapp.azure.com`
   * Example: `myapp-demo.eastus.cloudapp.azure.com`
3. Save the DNS settings.

Now you can access your app at `http://app.mydomain.com`.

---

## **Step 4 (Optional): Use HTTPS**

To secure with HTTPS:

* Use **Azure Application Gateway** or **NGINX Ingress Controller** (if on AKS).
* Get an SSL certificate (free via Let's Encrypt or from your domain provider).

---

### **Final URL**

Your application will now be accessible at:

```
http://myapp-demo.eastus.cloudapp.azure.com
```

Or your custom domain:

```
https://app.mydomain.com
```

---

---

# **Securing Your AKS App with TLS/HTTPS (NGINX Ingress)**

We’ll use **NGINX Ingress** + **Let’s Encrypt (cert-manager)** for automatic certificate management.

---

## **Step 1: Prerequisites**

* An **AKS cluster** with a **public IP** for the Ingress.
* A **domain name** pointing to that public IP (via an A record or CNAME).
* **kubectl** & **Helm** configured for your cluster.
* An existing **Ingress** for your application (without HTTPS yet).

---

## **Step 2: Install NGINX Ingress (if not already installed)**

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install nginx-ingress ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.ingressClassResource.name=nginx \
  --set controller.service.loadBalancerIP=<YOUR_PUBLIC_IP>
```

> **Replace `<YOUR_PUBLIC_IP>`** with the static IP you reserved for this Ingress.

---

## **Step 3: Install cert-manager (for TLS Certificates)**

```bash
helm repo add jetstack https://charts.jetstack.io
helm repo update

kubectl create namespace cert-manager

helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --set installCRDs=true
```

---

## **Step 4: Configure ClusterIssuer for Let’s Encrypt**

Create a **ClusterIssuer** (this requests certificates from Let’s Encrypt).

```yaml
# cluster-issuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v2.api.letsencrypt.org/directory
    email: your-email@example.com    # <-- replace with your email
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

Apply it:

```bash
kubectl apply -f cluster-issuer.yaml
```

---

## **Step 5: Update Your Ingress to Use TLS**

Modify your Ingress to include TLS:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-app
  namespace: default
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - app.mydomain.com         # <-- your domain
    secretName: my-app-tls     # <-- cert will be stored here
  rules:
  - host: app.mydomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: my-app-service
            port:
              number: 80
```

Apply it:

```bash
kubectl apply -f my-app-ingress.yaml
```

---

## **Step 6: Verify the Certificate**

Check if cert-manager issued the certificate:

```bash
kubectl get certificate -A
```

You should see:

```
NAME          READY   SECRET        AGE
my-app-tls    True    my-app-tls    1m
```

---

## **Step 7: Test**

Open your browser:

```
https://app.mydomain.com
```

You should now see a **valid Let’s Encrypt SSL certificate**.

---

### **Key Notes**

* Certificates **auto-renew** before expiry.
* For **staging/testing**, use Let’s Encrypt **staging** server to avoid rate limits:

  ```
  server: https://acme-staging-v02.api.letsencrypt.org/directory
  ```

---

### **What’s Next?**

* **Force HTTPS**: Add `nginx.ingress.kubernetes.io/force-ssl-redirect: "true"` annotation.
* **Custom Certificates**: If you have your own certs, you can manually create a TLS secret instead of using cert-manager.
* **Wildcard Domains**: Use DNS-01 challenge instead of HTTP-01 for `*.domain.com` certificates.

---




