package com.ubik.bookingservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class BookingRouter {

    @Bean
    public RouterFunction<ServerResponse> bookingRoutes(BookingHandler handler) {
        return RouterFunctions
            .route(GET("/api/bookings"), handler::listAll)
            .andRoute(GET("/api/bookings/{id}"), handler::findById)
            .andRoute(GET("/api/bookings/user/{userId}"), handler::findByUserId)
            .andRoute(POST("/api/bookings"), handler::create)
            .andRoute(PUT("/api/bookings/{id}/confirm"), handler::confirm)
            .andRoute(PUT("/api/bookings/{id}/cancel"), handler::cancel);
    }
}
