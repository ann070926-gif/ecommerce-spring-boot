# E-commerce Web Application (Spring Boot)

## Description
This project is a backend-focused e-commerce web application built using Spring Boot.  
It supports product browsing, shopping cart, order processing, and user authentication.

## Features
- User authentication (form login + Google OAuth2)
- Product listing and product details
- Shopping cart management
- Checkout and order processing
- Order history tracking
- Admin panel:
  - Manage products (CRUD)
  - Manage users
  - Manage categories
- Role-based authorization (admin / user)

## Backend Architecture
- Controller: handle HTTP requests
- Service: process business logic
- Repository: interact with database (JPA)

## Business Logic Flow
- Add to cart → store items in session
- Checkout → create order → save order details → update order status
- Authentication → validate user → assign role → authorize access

## Tech Stack
- Java (OOP)
- Spring Boot (MVC)
- Spring Security + OAuth2
- JPA / Hibernate
- SQL Server
- Thymeleaf

## Database
Main entities:
- User (Account)
- Product
- Category
- Order
- OrderDetail

## How to Run
1. Clone project:
2. 2. Open with IntelliJ / Eclipse
3. Configure database in `application.properties`
4. Run Spring Boot application
5. Access: http://localhost:8181

## Notes
- Project runs locally
- API tested using Postman
- Designed for learning backend and basic e-commerce flow

## Author
Ho Thanh An
