# 🛍️ Full-Stack E-Commerce Platform & Admin Dashboard

> Production-grade full-stack e-commerce application built with **Java 21**, **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA**, **PostgreSQL**, and **React**. Features catalog browsing, dynamic search/filters, atomic inventory reservation, cart management, and real-time order lifecycle tracking.

![Status: Development](https://img.shields.io/badge/Status-DEVELOPMENT-amber.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![React](https://img.shields.io/badge/React-18%2B-cyan.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)

---

## 📌 Project Overview

The **E-Commerce Platform & Admin Dashboard** is a full-featured online storefront and back-office management system. It provides a fluid customer experience for browsing hardware peripherals, managing persistent shopping carts, and placing orders, alongside an administrative control plane for catalog management, stock adjustments, and order state machine transitions.

> **⚠️ Payment Disclaimer**: Per portfolio design standards, no fake payment gateways or simulated credit card inputs are implemented. Checkout displays:  
> `Payment integration not configured`  
> Orders are safely recorded in the database, and product inventory is atomically reserved for full end-to-end demonstration.

---

## 🚀 Key Features

### 🛒 Customer Storefront
- **Catalog Navigation**: Browse curated products across multiple categories (Electronics, Audio, Wearables, Accessories).
- **Search & Filtering**: Search products by keyword, filter by category chips, and sort by price or date.
- **Cart Management**: Add items to cart with live inventory verification, quantity increment/decrement, and line-item removal.
- **Order Placement**: Atomic checkout verifying current stock levels, reserving inventory, and creating permanent order records.
- **Order Tracking**: Detailed customer order history with lifecycle statuses (`PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).

### 🛡️ Admin Dashboard & Operations
- **Business KPI Cards**: Total Revenue, Completed Orders, Active Catalog Items, and Low-Stock Warnings.
- **Product Management**: Create, edit, and soft-delete products with instant catalog synchronization.
- **Category Control**: Organize products into logical taxonomies.
- **Inventory Oversight**: Real-time stock level adjustments; orders marked as `CANCELLED` automatically return reserved stock back to active inventory.
- **Order Lifecycle Management**: Transition order states across the pipeline in real time.

---

## 🛠️ Technology Stack

| Component | Stack |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Data JPA, Spring Security, Hibernate |
| **Security** | Stateless JWT Authentication, BCrypt Password Hashing, RBAC |
| **Database** | PostgreSQL 16 (Primary), H2 Database (In-Memory Development / Testing) |
| **Build & Tooling** | Maven 3.9+, Lombok, Jakarta Validation |
| **API Documentation** | OpenAPI 3.0 / Swagger UI |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **Frontend** | React 18, Vite, Vanilla CSS (Modern E-Commerce Dark Theme), Lucide Icons |
| **DevOps** | Multi-Stage Dockerfile, Docker Compose |

---

## 🗄️ Relational Database Schema

```
+--------------------+            +------------------------+
|       users        | 1        1 |         carts          |
+--------------------+------------+------------------------+
| id (PK)            |            | id (PK)                |
| full_name          |            | user_id (FK, UNIQUE)   |
| email (UNIQUE)     |            +------------------------+
| password (HASHED)  |                        | 1
| role_id (FK)       |                        | *
| address            |            +------------------------+
+--------------------+            |       cart_items       |
         | 1                      +------------------------+
         |                        | id (PK)                |
         | *                      | cart_id (FK)           |
+--------------------+            | product_id (FK)        |
|       orders       |            | quantity               |
+--------------------+            | price (DECIMAL)        |
| id (PK)            |            +------------------------+
| order_number (UNQ) |                        | *
| user_id (FK)       |                        | 1
| total_amount       |            +------------------------+
| status (ENUM)      |            |        products        |
| shipping_address   |            +------------------------+
| payment_notice     |            | id (PK)                |
| created_at         |            | name, description      |
+--------------------+            | price (DECIMAL 12,2)   |
         | 1                      | stock_quantity (INT)   |
         | *                      | image_url              |
+--------------------+            | category_id (FK)       |
|    order_items     |            | active (BOOLEAN)       |
+--------------------+            +------------------------+
| id (PK)            |                        | *
| order_id (FK)      |                        | 1
| product_id (FK)    |            +------------------------+
| quantity           |            |       categories       |
| unit_price         |            +------------------------+
| subtotal           |            | id (PK)                |
+--------------------+            | name (UNIQUE), desc    |
                                  +------------------------+
```

---

## 📡 REST API Endpoints

### Public & Customer Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register customer account |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT token |
| `GET` | `/api/v1/products` | Browse & search products with pagination |
| `GET` | `/api/v1/products/{id}` | Get product details |
| `GET` | `/api/v1/categories` | Get category taxonomy |
| `GET` | `/api/v1/cart` | Get authenticated user's cart |
| `POST` | `/api/v1/cart/items` | Add item to cart with stock validation |
| `PUT` | `/api/v1/cart/items/{id}` | Update quantity |
| `DELETE`| `/api/v1/cart/items/{id}` | Remove item from cart |
| `POST` | `/api/v1/orders` | Checkout cart & create order |
| `GET` | `/api/v1/orders` | Customer order history |

### Admin Endpoints (`/api/v1/admin`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/admin/dashboard` | KPI analytics (sales, orders, stock) |
| `POST` | `/api/v1/admin/products` | Publish new product to catalog |
| `PUT` | `/api/v1/admin/products/{id}` | Update existing product details |
| `DELETE`| `/api/v1/admin/products/{id}` | Deactivate product (soft-delete) |
| `POST` | `/api/v1/admin/categories` | Add new category |
| `PUT` | `/api/v1/admin/products/{id}/inventory` | Adjust stock count |
| `GET` | `/api/v1/admin/orders` | View all customer orders |
| `PUT` | `/api/v1/admin/orders/{id}/status` | Update order state |

---

## 💻 Local Setup & Execution

### 1. Backend Setup
```bash
cd backend

# Run automated tests
mvn test

# Run Spring Boot backend (port 8081)
mvn spring-boot:run
```

Seed credentials:
- **Admin**: `admin@store.com` / `AdminPassword@123`
- **Customer**: `customer@store.com` / `CustomerPassword@123`

### 2. Frontend Setup
```bash
cd frontend

npm install
npm run dev
```
Open `http://localhost:5173`.

---

## 🐳 Docker Deployment

```bash
docker-compose up --build
```
Spins up PostgreSQL 16 on port `5433` and Spring Boot backend on port `8081`.

---

## 📖 Swagger / OpenAPI Specification
Access the interactive OpenAPI UI at:
```
http://localhost:8081/swagger-ui.html
```

---

## 👨‍💻 Author
**Praveenkumar T**  
- Portfolio: Coming Soon
- GitHub: [githubUrl: ""](Coming Soon)
- Live Demo: [liveUrl: ""](Coming Soon)
