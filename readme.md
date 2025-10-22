# 🏦 Bank Simulator

A **full-stack banking management system** that simulates essential banking operations such as customer onboarding, account management, authentication, and secure fund transactions.
This project uses a **Java (Jersey + MySQL)** backend and a **React (Vite + TypeScript + Tailwind)** frontend.

---

## 📋 Table of Contents

1. [Introduction](#introduction)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Features](#features)
5. [Installation](#installation)
6. [Configuration](#configuration)
7. [Usage](#usage)
8. [API Endpoints](#api-endpoints)
9. [Frontend Setup](#frontend-setup)
10. [Testing](#testing)
11. [Troubleshooting](#troubleshooting)
12. [License](#license)

---

## 🧭 Introduction

**Bank Simulator** is a comprehensive RESTful application that mimics the operations of a modern digital banking platform.
It allows admins or system operators to:

* Create and manage customer accounts
* Handle transactions with validation and error handling
* Manage user authentication securely
* Export transaction records to Excel

---

## ⚙️ Tech Stack

### **Backend**

* **Language:** Java 22
* **Framework:** Jersey (Jakarta RESTful Web Services 3.1.3)
* **Database:** MySQL
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito
* **Excel Generation:** Apache POI
* **Mailing:** Jakarta Mail

### **Frontend**

* **Framework:** React (Vite + TypeScript)
* **Styling:** Tailwind CSS
* **UI Components:** Shadcn/UI
* **Linting:** ESLint

---

## 📁 Project Structure

```
shreyashs19-without-valid/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/bank/simulator/
│   │   │   ├── config/           # App configs (DB, CORS, Rest API setup)
│   │   │   ├── controller/       # REST endpoints
│   │   │   ├── model/            # Data models
│   │   │   ├── service/          # Business logic & interfaces
│   │   │   ├── service/impl/     # Implementations
│   │   │   └── validation/       # Data validators
│   │   └── test/java/...         # Unit tests
│   └── webapp/WEB-INF/web.xml
│
└── frontend/
    ├── src/
    │   ├── pages/                # Page views (Dashboard, Login, Accounts)
    │   ├── components/           # UI components and modals
    │   ├── services/             # API calls to backend
    │   └── utils/                # Helpers (Excel export, toasts, etc.)
    ├── public/
    ├── package.json
    └── vite.config.ts
```

---

## 🚀 Features

### Backend

* ✅ RESTful API built with **Jersey**
* ✅ **Authentication** with email/password
* ✅ CRUD operations for **Customers**, **Accounts**, and **Transactions**
* ✅ **Excel export** for transaction history
* ✅ Input **validation and error handling**
* ✅ **CORS** enabled for frontend integration
* ✅ **Automatic database initialization**

### Frontend

* 💡 Interactive dashboard for managing customers and accounts
* 📊 Transaction viewing and export options
* 🔐 Login and signup flow
* 🌙 Responsive UI with Tailwind CSS
* 🧩 Modular and reusable component design

---

## ⚙️ Installation

### Prerequisites

* Java 22+
* Maven 3.9+
* Node.js 18+
* MySQL Server running locally (default port 3306)

### Clone Repository

```bash
git clone https://github.com/your-username/bank-simulator.git
cd bank-simulator
```

---

## 🛠️ Backend Setup

1. Navigate to the backend folder:

   ```bash
   cd backend
   ```

2. Create a configuration file:

   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

3. Update the DB credentials in `application.properties`:

   ```properties
   db.url=jdbc:mysql://localhost:3306/bank_simulation?useSSL=false&serverTimezone=UTC
   db.username=root
   db.password=yourpassword
   ```

4. Build the WAR package:

   ```bash
   mvn clean package
   ```

5. Deploy on Tomcat or run with an embedded server:

   ```bash
   mvn jetty:run
   ```

6. The API will be available at:

   ```
   http://localhost:8080/api
   ```

---

## 🌐 Frontend Setup

1. Move to the frontend directory:

   ```bash
   cd frontend
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Create `.env` file from example:

   ```bash
   cp .env.example .env
   ```

   Set your backend API URL:

   ```env
   VITE_API_URL=http://localhost:8080/api
   ```

4. Run the development server:

   ```bash
   npm run dev
   ```

5. Open your browser at:

   ```
   http://localhost:5173
   ```

---

## 🧪 Testing

To execute all unit tests:

```bash
cd backend
mvn test
```

JUnit and Mockito are used for testing services, controllers, and validations.

---

## 🧰 API Endpoints

| Method     | Endpoint                               | Description                        |
| ---------- | -------------------------------------- | ---------------------------------- |
| **POST**   | `/api/auth/signup`                     | Register a new user                |
| **POST**   | `/api/auth/login`                      | Authenticate a user                |
| **POST**   | `/api/customer/onboard`                | Create a new customer              |
| **GET**    | `/api/customer/all`                    | Get all customers                  |
| **PUT**    | `/api/customer/aadhar/{aadharNumber}`  | Update customer by Aadhar          |
| **DELETE** | `/api/customer/aadhar/{aadharNumber}`  | Delete customer                    |
| **POST**   | `/api/account/add`                     | Create new account                 |
| **GET**    | `/api/account/all`                     | List all accounts                  |
| **PUT**    | `/api/account/number/{account_number}` | Update account by number           |
| **DELETE** | `/api/account/number/{account_number}` | Delete account                     |
| **POST**   | `/api/transaction/createTransaction`   | Create a transaction               |
| **GET**    | `/api/transaction/all`                 | Fetch all transactions             |
| **GET**    | `/api/transaction/download/all`        | Download all transactions as Excel |

---

## ⚙️ Troubleshooting

| Issue                                | Solution                                                        |
| ------------------------------------ | --------------------------------------------------------------- |
| `MySQL JDBC Driver not found`        | Ensure MySQL connector JAR is available or configured via Maven |
| `CORS blocked`                       | Verify `CorsFilter.java` is active under `@Provider`            |
| `Port already in use`                | Change server port in `application.properties`                  |
| `Frontend not connecting to backend` | Check `.env` variable `VITE_API_URL`                            |

---



## 📜 License

This project is licensed under the **MIT License** — see the `LICENSE` file for details.
