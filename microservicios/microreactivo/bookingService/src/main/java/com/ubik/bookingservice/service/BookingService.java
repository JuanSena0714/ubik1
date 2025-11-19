package com.ubik.bookingservice.service;

import com.ubik.bookingservice.domain.Booking;
import com.ubik.bookingservice.dto.*;
import com.ubik.bookingservice.repo.BookingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MotelServiceClient motelServiceClient;

    public BookingService(BookingRepository bookingRepository, MotelServiceClient motelServiceClient) {
        this.bookingRepository = bookingRepository;
        this.motelServiceClient = motelServiceClient;
    }

    public Mono<BookingResponse> createBooking(BookingRequest request) {
        // Validar que la fecha de salida sea posterior a la fecha de entrada
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            return Mono.error(new IllegalArgumentException("Check-out date must be after check-in date"));
        }

        // Verificar que la habitación esté disponible
        return motelServiceClient.getRoomById(request.roomId())
            .flatMap(room -> {
                if (!room.available()) {
                    return Mono.error(new RuntimeException("Room is not available"));
                }

                // Calcular el precio total
                long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());

                // Validar que haya al menos una noche
                if (nights < 1) {
                    return Mono.error(new IllegalArgumentException("Booking must be for at least one night"));
                }
                BigDecimal totalPrice = room.pricePerNight().multiply(BigDecimal.valueOf(nights));

                // Crear la reserva
                Booking booking = new Booking(
                    null,
                    request.userId(),
                    request.roomId(),
                    request.motelId(),
                    request.checkInDate(),
                    request.checkOutDate(),
                    totalPrice,
                    "PENDING",
                    request.guestName(),
                    request.guestEmail(),
                    request.guestPhone(),
                    request.specialRequests(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
                );

                return bookingRepository.save(booking)
                    .flatMap(savedBooking ->
                        // Marcar la habitación como no disponible
                        motelServiceClient.updateRoomAvailability(request.roomId(), false)
                            .flatMap(updatedRoom ->
                                // Obtener información del motel para la respuesta
                                motelServiceClient.getMotelById(request.motelId())
                                    .map(motel -> new BookingResponse(
                                        savedBooking.id(),
                                        savedBooking.userId(),
                                        savedBooking.roomId(),
                                        savedBooking.motelId(),
                                        savedBooking.checkInDate(),
                                        savedBooking.checkOutDate(),
                                        savedBooking.totalPrice(),
                                        savedBooking.status(),
                                        savedBooking.guestName(),
                                        savedBooking.guestEmail(),
                                        savedBooking.guestPhone(),
                                        savedBooking.specialRequests(),
                                        savedBooking.createdAt(),
                                        savedBooking.updatedAt(),
                                        motel.name(),
                                        updatedRoom.roomNumber(),
                                        updatedRoom.roomType()
                                    ))
                            )
                    );
            });
    }

    public Mono<BookingResponse> confirmBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .flatMap(booking -> {
                Booking updated = new Booking(
                    booking.id(),
                    booking.userId(),
                    booking.roomId(),
                    booking.motelId(),
                    booking.checkInDate(),
                    booking.checkOutDate(),
                    booking.totalPrice(),
                    "CONFIRMED",
                    booking.guestName(),
                    booking.guestEmail(),
                    booking.guestPhone(),
                    booking.specialRequests(),
                    booking.createdAt(),
                    LocalDateTime.now()
                );
                return bookingRepository.save(updated)
                    .flatMap(this::buildBookingResponse);
            });
    }

    public Mono<BookingResponse> cancelBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .flatMap(booking -> {
                Booking updated = new Booking(
                    booking.id(),
                    booking.userId(),
                    booking.roomId(),
                    booking.motelId(),
                    booking.checkInDate(),
                    booking.checkOutDate(),
                    booking.totalPrice(),
                    "CANCELLED",
                    booking.guestName(),
                    booking.guestEmail(),
                    booking.guestPhone(),
                    booking.specialRequests(),
                    booking.createdAt(),
                    LocalDateTime.now()
                );
                return bookingRepository.save(updated)
                    .flatMap(savedBooking ->
                        // Marcar la habitación como disponible nuevamente
                        motelServiceClient.updateRoomAvailability(booking.roomId(), true)
                            .then(buildBookingResponse(savedBooking))
                    );
            });
    }

    public Flux<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
            .flatMap(this::buildBookingResponse);
    }

    public Mono<BookingResponse> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .flatMap(this::buildBookingResponse);
    }

    public Flux<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
            .flatMap(this::buildBookingResponse);
    }

    private Mono<BookingResponse> buildBookingResponse(Booking booking) {
        return motelServiceClient.getRoomById(booking.roomId())
            .zipWith(motelServiceClient.getMotelById(booking.motelId()))
            .map(tuple -> {
                RoomDTO room = tuple.getT1();
                MotelDTO motel = tuple.getT2();
                return new BookingResponse(
                    booking.id(),
                    booking.userId(),
                    booking.roomId(),
                    booking.motelId(),
                    booking.checkInDate(),
                    booking.checkOutDate(),
                    booking.totalPrice(),
                    booking.status(),
                    booking.guestName(),
                    booking.guestEmail(),
                    booking.guestPhone(),
                    booking.specialRequests(),
                    booking.createdAt(),
                    booking.updatedAt(),
                    motel.name(),
                    room.roomNumber(),
                    room.roomType()
                );
            });
    }
}
