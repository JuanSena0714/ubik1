package com.ubik.bookingservice.repo;

import com.ubik.bookingservice.domain.Booking;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BookingRepository extends ReactiveCrudRepository<Booking, Long> {
    Flux<Booking> findByUserId(Long userId);
    Flux<Booking> findByRoomId(Long roomId);
    Flux<Booking> findByMotelId(Long motelId);
    Flux<Booking> findByStatus(String status);
}
