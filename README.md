# Shop Service Backend
- Full CRUD operations for **Orders** and **Products** via REST endpoints
- - **Inventory** management (goods in / goods out)
- Clean separation of data, logic, and repository layers
- Calculates total price based on Product × Quantity
- Uses BigDecimal for accurate currency computation
- Robust error handling with custom exceptions and proper HTTP status codes
- MongoDB persistence with Spring Data repositories
- Embedded MongoDB for fast, isolated integration tests
- Comprehensive unit and integration tests covering controllers and services
- Configurable via environment variables (e.g. `SPRING_DATA_MONGODB_URI`)
- Built with Spring Boot, Lombok, and Maven


# Technologies

[![My Skills](https://skillicons.dev/icons?i=java,maven,spring,mongodb&perline=4)](https://skillicons.dev)

# Local development

1. Clone the repository
2. Run
```
  export SPRING_DATA_MONGODB_URI="mongodb+srv://<username>:<password>@your-cluster.mongodb.net/yourDb?retryWrites=true&w=majority"
  mvn spring-boot:run
```

# Explore the REST API

### Orders
List all orders
```
http GET :8080/orders
```

List orders by status
```
http GET :8080/orders status==COMPLETED
```

Get a single order by ID
```
http GET :8080/orders/abcd1234
```

Place a new order
```
  http POST :8080/orders \
  id=newOrder \
  status=PROCESSING \
  items:='[
    {
      "product": {
        "id": "P1",
        "name": "T-Shirt",
        "brand": "ACME Apparel",
        "description": "100% cotton crew neck",
        "color": "Red",
        "size": "M",
        "price": 19.99,
        "stock": 50
      },
      "quantity": 2
    }
  ]'
  ```
Update order status
```
http DELETE :8080/orders/abcd1234
```
Cancel (delete) an order
```
http DELETE :8080/orders/abcd1234
```

### Products
Create a product
```
http POST :8080/products \
  id=P3 \
  name="Baseball Cap" \
  brand="HeadGear" \
  description="Classic unisex cap" \
  color="Blue" \
  size="One-Size" \
  price:=14.95 \
  stock:=100
```
List all product
```
http GET :8080/products
```
Get a product by ID
```
http GET :8080/products/P3
```
Update a product
```
http PUT :8080/products/P3 \
  name="Embroidered Baseball Cap" \
  brand="HeadGear" \
  description="Cap with front logo" \
  color="Navy" \
  size="One-Size" \
  price:=16.95 \
  stock:=120
```
Delete a product
```
http DELETE :8080/products/P3
```

### Inventory
Goods in (increase stock)
```
http POST :8080/inventory/in \
  productId==P1 \
  amount==20
```
Goods out (decrease stock)
```
http POST :8080/inventory/out \
  productId==P1 \
  amount==5
```

# Testing
### Run all tests:
```
mvn test
```
### Run a single test class:
```
mvn -Dtest=OrderControllerTest test
```


