package com.ubik.bookingservice.web;

import com.ubik.bookingservice.dto.BookingRequest;
import com.ubik.bookingservice.dto.BookingResponse;
import com.ubik.bookingservice.service.BookingService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BookingHandler {

    private final BookingService bookingService;

    public BookingHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public Mono<ServerResponse> listAll(ServerRequest req) {
        return ServerResponse.ok().body(bookingService.getAllBookings(), BookingResponse.class);
    }

    public Mono<ServerResponse> findById(ServerRequest req) {
        Long id = Long.valueOf(req.pathVariable("id"));
        return bookingService.getBookingById(id)
            .flatMap(booking -> ServerResponse.ok().bodyValue(booking))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByUserId(ServerRequest req) {
        Long userId = Long.valueOf(req.pathVariable("userId"));
        return ServerResponse.ok().body(bookingService.getBookingsByUserId(userId), BookingResponse.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(BookingRequest.class)
            .flatMap(bookingService::createBooking)
            .flatMap(booking -> ServerResponse.ok().bodyValue(booking))
            .onErrorResume(error ->
                ServerResponse.badRequest().bodyValue("Error creating booking: " + error.getMessage())
            );
    }

    public Mono<ServerResponse> confirm(ServerRequest req) {
        Long id = Long.valueOf(req.pathVariable("id"));
        return bookingService.confirmBooking(id)
            .flatMap(booking -> ServerResponse.ok().bodyValue(booking))
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(error ->
                ServerResponse.badRequest().bodyValue("Error confirming booking: " + error.getMessage())
            );
    }

    public Mono<ServerResponse> cancel(ServerRequest req) {
        Long id = Long.valueOf(req.pathVariable("id"));
        return bookingService.cancelBooking(id)
            .flatMap(booking -> ServerResponse.ok().bodyValue(booking))
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(error ->
                ServerResponse.badRequest().bodyValue("Error cancelling booking: " + error.getMessage())
            );
    }
}
