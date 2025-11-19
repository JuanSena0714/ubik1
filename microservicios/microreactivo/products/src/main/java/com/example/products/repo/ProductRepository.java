package com.example.products.repo;
import com.example.products.domain.Product; import org.springframework.data.repository.reactive.ReactiveCrudRepository;
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> { }
