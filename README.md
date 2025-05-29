# Shop Service Backend
- Calculates total price based on Product x Quantity
- Uses BigDecimal for accurate currency computation
- Clean separation of data, logic, and repository layers

# Technologies

[![Java](https://skillicons.dev/icons?i=java)](https://openjdk.org/)  
[![Spring Boot](https://skillicons.dev/icons?i=spring)](https://spring.io/projects/spring-boot)
[![MongoDB](https://skillicons.dev/icons?i=mongodb)](https://www.mongodb.com/) 
[![Maven](https://skillicons.dev/icons?i=maven)](https://maven.apache.org/)  

### Local development

- Clone the repository
- Run Main.java

### Explore the REST API

List all orders
`http GET :8080/orders`

List orders by status
`http GET :8080/orders status==COMPLETED`

### Testing

# products
curl -X POST   http://localhost:8080/products   -d '{...}' -H 'Content-Type: application/json'
curl http://localhost:8080/products
curl http://localhost:8080/products/{id}
curl -X PUT    http://localhost:8080/products/{id}   -d '{...}'
curl -X DELETE http://localhost:8080/products/{id}


