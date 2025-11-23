# [Project Name] - Enterprise Quiz System

A cloud-native backend system built with **Spring Boot** and **AWS**, designed for high security and scalability.

## 🏗 Architecture
Unlike standard tutorials, this project implements a **Private VPC Network** strategy to secure user data. The database is isolated in a private subnet, inaccessible from the public internet, accessed only via a secure VPC Connector.

![Architecture Diagram](quiz-test.drawio.png)

### ☁️ Cloud Infrastructure (AWS)
* **Compute:** AWS App Runner (Containerized Spring Boot)
* **Database:** AWS RDS PostgreSQL (Private Subnet)
* **Security:** * Custom **VPC Connector** for secure tunneling.
    * Strict **Security Group** rules (Ingress restricted to App Runner only).
    * **Environment Parity** using dynamic configuration injection.

## 🛠 Tech Stack
* **Java 17 & Spring Boot 3**
* **Spring Security & JWT** (Stateless Auth)
* **Docker** (Containerization)
* **GitHub Actions** (CI/CD Pipeline)
* **Terraform** (Infrastructure as Code - Optional if you add this later)
