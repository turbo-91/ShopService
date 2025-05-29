# Shop Service Backend
- Calculates total price based on Product x Quantity
- Uses BigDecimal for accurate currency computation
- Clean separation of data, logic, and repository layers

# Technologies

[![My Skills](https://skillicons.dev/icons?i=java,maven,spring,mongodb&perline=4)](https://skillicons.dev)

# Local development

- Clone the repository
- Run Main.java

# Explore the REST API

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

### Testing

# products
curl -X POST   http://localhost:8080/products   -d '{...}' -H 'Content-Type: application/json'
curl http://localhost:8080/products
curl http://localhost:8080/products/{id}
curl -X PUT    http://localhost:8080/products/{id}   -d '{...}'
curl -X DELETE http://localhost:8080/products/{id}


