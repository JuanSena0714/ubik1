# microreactivo-2025

Plantilla 2025 con 3 microservicios **Spring WebFlux** (funcional con `records`) + **API Gateway** (Spring Cloud Gateway Server WebFlux) y **R2DBC MySQL**.

## Requisitos
- JDK 21
- Maven 3.9+
- MySQL 8+

## Preparación de MySQL
```sql
-- ejecuta en tu MySQL local:
SOURCE mysql-init.sql;
```

## Cómo ejecutar (sin Docker)
En terminales separadas:
```bash
mvn -q -pl MotelManagemement -am spring-boot:run
mvn -q -pl products  -am spring-boot:run
mvn -q -pl orders    -am spring-boot:run
mvn -q -pl gateway   -am spring-boot:run
```

Gateway: `http://localhost:8080/api/products` etc.

## Pruebas
```bash
mvn -q -pl customers test
mvn -q -pl products  test
mvn -q -pl orders    test
mvn -q -pl gateway   test
```
