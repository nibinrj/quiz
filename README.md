# Quiz Application README

## 1\. Project Overview

This is a quiz application built with Java and the Spring Boot framework. It provides a platform for both students and administrators to interact with quizzes. The application features user authentication and authorization using JSON Web Tokens (JWT) and role-based access control.

-----

## 2\. Features

  * **User Registration and Login:** Users can register for an account and log in to the application.
  * **JWT Authentication:** The application uses JWT for secure authentication.
  * **Role-Based Access Control:** There are two user roles:
      * **ADMIN:** Can add and delete quiz questions.
      * **STUDENT:** Can view quiz questions.
  * **Quiz Management (Admin):** Admins can add new questions with options and answers to the quiz and delete existing questions.
  * **Quiz Viewing (Student):** Students can view all the available quiz questions.

-----

## 3\. Technologies Used

  * **Java:** The core programming language.
  * **Spring Boot:** Framework for creating stand-alone, production-grade Spring-based Applications.
  * **Spring Security:** For authentication and access control.
  * **JSON Web Tokens (JWT):** For stateless authentication.
  * **Spring Data JPA (Hibernate):** For data persistence.
  * **PostgreSQL:** The relational database used to store application data.
  * **Maven:** For project build and dependency management.
  * **Lombok:** To reduce boilerplate code.

-----

## 4\. API Endpoints

### 4.1. Authentication

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/register` | Registers a new user. |
| `POST` | `/login` | Authenticates a user and returns a JWT. |

### 4.2. Admin

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/admin/add` | Adds a new quiz question. |
| `DELETE` | `/admin/delete/{id}` | Deletes a quiz question by its ID. |

### 4.3. Student

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/student/getall` | Retrieves all quiz questions. |

-----

## 5\. Project Structure

The project follows a standard Maven project structure:

```
.
├── .mvn
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── nibin
│   │   │           └── quiz
│   │   │               ├── Controller
│   │   │               ├── Model
│   │   │               ├── Repository
│   │   │               ├── Service
│   │   │               ├── config
│   │   │               └── QuizApplication.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── nibin
│                   └── quiz
│                       └── QuizApplicationTests.java
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

-----

## 6\. Dependencies

The main dependencies are listed in the `pom.xml` file and include:

  * `spring-boot-starter-data-jpa`
  * `spring-boot-starter-web`
  * `spring-boot-starter-security`
  * `postgresql`
  * `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
  * `lombok`
