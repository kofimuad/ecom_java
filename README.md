# Fresh Market — E-Commerce Backend

A RESTful backend API for an e-commerce platform built with Spring Boot and PostgreSQL. Handles users, products, categories, shopping carts, and orders.

---

## Prerequisites

Make sure you have the following installed before running the project:

- Java 17
- Maven
- PostgreSQL

---

## Database Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE ecom_db;
```

2. Create a database user and grant access:

```sql
CREATE USER your_db_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ecom_db TO your_db_user;
```

3. Run your table creation SQL scripts to set up the schema (users, categories, products, product_images, carts, cart_items, orders, order_items).

---

## Configuration

The app reads database credentials from environment variables. Create a `.env` file at the project root with the following content:

```env
DB_URL=jdbc:postgresql://localhost:5432/ecom_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_password
```

The `application.properties` file references these variables using `${DB_URL}`, `${DB_USERNAME}`, and `${DB_PASSWORD}`. Never commit your `.env` file — it is already listed in `.gitignore`.

---

## Running the Application

Since Spring Boot does not automatically read `.env` files, you need to load the environment variables into your shell before starting the app.

**Option 1 — Terminal (recommended):**

```bash
export $(cat .env | xargs) && mvn spring-boot:run
```

**Option 2 — IntelliJ IDEA:**

Go to `Run > Edit Configurations`, select your Spring Boot run configuration, and add the following under `Environment variables`:

```
DB_URL=jdbc:postgresql://localhost:5432/ecom_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_password
```

Then run the app normally.

The server starts on `http://localhost:8080`.

---

## Swagger UI

Once the app is running, visit the Swagger UI to explore and test all endpoints interactively:

```
http://localhost:8080/swagger-ui/index.html
```

The full OpenAPI spec is available at:

```
http://localhost:8080/v3/api-docs
```

---

## API Endpoints

### Users — `/api/users`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create a new user |
| PUT | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Delete a user |

### Categories — `/api`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/public/categories` | Get all categories |
| POST | `/api/public/categories` | Create a category |
| PUT | `/api/admin/categories/{id}` | Update a category |
| DELETE | `/api/admin/categories/{id}` | Delete a category |

### Products — `/api/public/products`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/public/products` | Get all products |
| GET | `/api/public/products/{id}` | Get product by ID |
| POST | `/api/public/products` | Create a product |
| PUT | `/api/public/products/{id}` | Update a product |
| DELETE | `/api/public/products/{id}` | Delete a product |

### Product Images — `/api/products/{productId}/images`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/{productId}/images` | Get images for a product |
| POST | `/api/products/{productId}/images` | Add an image to a product |
| DELETE | `/api/products/{productId}/images/{imageId}` | Delete a product image |

### Cart — `/api/cart`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart/user/{userId}` | Get or create cart for a user |
| GET | `/api/cart/{cartId}` | Get cart by ID |
| GET | `/api/cart/{cartId}/items` | Get all items in a cart |
| GET | `/api/cart/{cartId}/total` | Get cart total |
| POST | `/api/cart/{cartId}/items` | Add or increment an item in the cart |
| PATCH | `/api/cart/items/{cartItemId}` | Update cart item quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Remove an item from the cart |
| DELETE | `/api/cart/{cartId}/clear` | Clear all items from the cart |

### Orders — `/api/orders`
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders/checkout` | Place an order from a cart |
| GET | `/api/orders/{orderId}` | Get order by ID |
| GET | `/api/orders/user/{userId}` | Get all orders for a user |
| GET | `/api/orders/{orderId}/items` | Get items in an order |
| PATCH | `/api/orders/{orderId}/status` | Update order status |
| PATCH | `/api/orders/{orderId}/cancel` | Cancel an order |

---

## Tech Stack

- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Database Access:** Spring JdbcTemplate
- **Password Hashing:** BCrypt
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Build Tool:** Maven