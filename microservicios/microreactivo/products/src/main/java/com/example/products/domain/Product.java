package com.example.products.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.math.BigDecimal;

@Table("products")
public record Product(@Id Long id, @Column("name") String name, @Column("price") BigDecimal price, @Column("stock") BigDecimal stock) {}
