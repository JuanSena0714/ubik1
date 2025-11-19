package com.example.products.web;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.web.reactive.function.server.RouterFunction; import org.springframework.web.reactive.function.server.RouterFunctions; import org.springframework.web.reactive.function.server.ServerResponse; import static org.springframework.web.reactive.function.server.RequestPredicates.*;
@Configuration
public class ProductRouter {
  @Bean RouterFunction<ServerResponse> routes(ProductHandler h){
    return RouterFunctions.route(GET("/products"),h::all)
      .andRoute(GET("/products/{id}"),h::byId)
      .andRoute(POST("/products"),h::create)
      .andRoute(PUT("/products/{id}"),h::update)
      .andRoute(DELETE("/products/{id}"),h::delete);
  }
}
