package com.ubik.bookingservice;

import com.ubik.bookingservice.domain.Booking;
import com.ubik.bookingservice.dto.*;
import com.ubik.bookingservice.repo.BookingRepository;
import com.ubik.bookingservice.service.BookingService;
import com.ubik.bookingservice.service.MotelServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MotelServiceClient motelServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private RoomDTO availableRoom;
    private RoomDTO unavailableRoom;
    private MotelDTO motel;
    private BookingRequest validRequest;
    private Booking savedBooking;

    @BeforeEach
    void setUp() {
        // Setup available room
        availableRoom = new RoomDTO(
            1L, 1L, "101", "Suite",
            new BigDecimal("100.00"), 2, true, "Luxury suite"
        );

        // Setup unavailable room
        unavailableRoom = new RoomDTO(
            2L, 1L, "102", "Standard",
            new BigDecimal("50.00"), 2, false, "Standard room"
        );

        // Setup motel
        motel = new MotelDTO(
            1L, "Grand Motel", "123 Main St", "555-1234",
            4.5, "Great motel"
        );

        // Setup valid booking request
        validRequest = new BookingRequest(
            1L, // userId
            1L, // roomId
            1L, // motelId
            LocalDate.now().plusDays(1), // checkInDate
            LocalDate.now().plusDays(3), // checkOutDate (2 nights)
            "John Doe",
            "john@example.com",
            "555-0000",
            "Late check-in please"
        );

        // Setup saved booking
        savedBooking = new Booking(
            1L, 1L, 1L, 1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3),
            new BigDecimal("200.00"),
            "PENDING",
            "John Doe",
            "john@example.com",
            "555-0000",
            "Late check-in please",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void createBooking_withAvailableRoom_shouldSucceed() {
        // Arrange
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));
        when(motelServiceClient.updateRoomAvailability(1L, false))
            .thenReturn(Mono.just(new RoomDTO(1L, 1L, "101", "Suite",
                new BigDecimal("100.00"), 2, false, "Luxury suite")));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.createBooking(validRequest))
            .expectNextMatches(response ->
                response.status().equals("PENDING") &&
                response.totalPrice().compareTo(new BigDecimal("200.00")) == 0 &&
                response.guestName().equals("John Doe")
            )
            .verifyComplete();

        verify(motelServiceClient).updateRoomAvailability(1L, false);
    }

    @Test
    void createBooking_withUnavailableRoom_shouldFail() {
        // Arrange
        when(motelServiceClient.getRoomById(2L)).thenReturn(Mono.just(unavailableRoom));

        BookingRequest requestWithUnavailableRoom = new BookingRequest(
            1L, 2L, 1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3),
            "John Doe", "john@example.com", "555-0000", null
        );

        // Act & Assert
        StepVerifier.create(bookingService.createBooking(requestWithUnavailableRoom))
            .expectErrorMatches(error ->
                error instanceof RuntimeException &&
                error.getMessage().contains("not available")
            )
            .verify();
    }

    @Test
    void createBooking_withInvalidDates_shouldFail() {
        // Arrange - checkout before checkin
        BookingRequest invalidRequest = new BookingRequest(
            1L, 1L, 1L,
            LocalDate.now().plusDays(3), // checkIn
            LocalDate.now().plusDays(1), // checkOut (before checkIn!)
            "John Doe", "john@example.com", "555-0000", null
        );

        // Act & Assert
        StepVerifier.create(bookingService.createBooking(invalidRequest))
            .expectErrorMatches(error ->
                error instanceof IllegalArgumentException &&
                error.getMessage().contains("after check-in date")
            )
            .verify();
    }

    @Test
    void createBooking_withSameCheckInAndCheckOut_shouldFail() {
        // Arrange
        LocalDate sameDate = LocalDate.now().plusDays(1);
        BookingRequest invalidRequest = new BookingRequest(
            1L, 1L, 1L,
            sameDate, sameDate,
            "John Doe", "john@example.com", "555-0000", null
        );

        // Act & Assert
        StepVerifier.create(bookingService.createBooking(invalidRequest))
            .expectErrorMatches(error ->
                error instanceof IllegalArgumentException &&
                error.getMessage().contains("after check-in date")
            )
            .verify();
    }

    @Test
    void confirmBooking_withValidId_shouldUpdateStatus() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Mono.just(savedBooking));

        Booking confirmedBooking = new Booking(
            savedBooking.id(), savedBooking.userId(), savedBooking.roomId(),
            savedBooking.motelId(), savedBooking.checkInDate(), savedBooking.checkOutDate(),
            savedBooking.totalPrice(), "CONFIRMED", savedBooking.guestName(),
            savedBooking.guestEmail(), savedBooking.guestPhone(),
            savedBooking.specialRequests(), savedBooking.createdAt(),
            LocalDateTime.now()
        );

        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(confirmedBooking));
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.confirmBooking(1L))
            .expectNextMatches(response -> response.status().equals("CONFIRMED"))
            .verifyComplete();
    }

    @Test
    void cancelBooking_withValidId_shouldUpdateStatusAndFreeRoom() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Mono.just(savedBooking));

        Booking cancelledBooking = new Booking(
            savedBooking.id(), savedBooking.userId(), savedBooking.roomId(),
            savedBooking.motelId(), savedBooking.checkInDate(), savedBooking.checkOutDate(),
            savedBooking.totalPrice(), "CANCELLED", savedBooking.guestName(),
            savedBooking.guestEmail(), savedBooking.guestPhone(),
            savedBooking.specialRequests(), savedBooking.createdAt(),
            LocalDateTime.now()
        );

        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(cancelledBooking));
        when(motelServiceClient.updateRoomAvailability(1L, true))
            .thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.cancelBooking(1L))
            .expectNextMatches(response -> response.status().equals("CANCELLED"))
            .verifyComplete();

        verify(motelServiceClient).updateRoomAvailability(1L, true);
    }

    @Test
    void getBookingById_withValidId_shouldReturnBooking() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Mono.just(savedBooking));
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.getBookingById(1L))
            .expectNextMatches(response ->
                response.id().equals(1L) &&
                response.guestName().equals("John Doe")
            )
            .verifyComplete();
    }

    @Test
    void getBookingsByUserId_shouldReturnUserBookings() {
        // Arrange
        when(bookingRepository.findByUserId(1L)).thenReturn(Flux.just(savedBooking));
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.getBookingsByUserId(1L))
            .expectNextMatches(response -> response.userId().equals(1L))
            .verifyComplete();
    }

    @Test
    void getAllBookings_shouldReturnAllBookings() {
        // Arrange
        Booking booking2 = new Booking(
            2L, 2L, 1L, 1L,
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(7),
            new BigDecimal("200.00"),
            "CONFIRMED",
            "Jane Smith",
            "jane@example.com",
            "555-1111",
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(bookingRepository.findAll()).thenReturn(Flux.just(savedBooking, booking2));
        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.getAllBookings())
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void createBooking_shouldCalculateCorrectPrice() {
        // Arrange - 5 nights at $100/night = $500
        BookingRequest fiveNightRequest = new BookingRequest(
            1L, 1L, 1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(6), // 5 nights
            "John Doe", "john@example.com", "555-0000", null
        );

        Booking fiveNightBooking = new Booking(
            1L, 1L, 1L, 1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(6),
            new BigDecimal("500.00"), // 5 * 100
            "PENDING",
            "John Doe", "john@example.com", "555-0000", null,
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(motelServiceClient.getRoomById(1L)).thenReturn(Mono.just(availableRoom));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(fiveNightBooking));
        when(motelServiceClient.updateRoomAvailability(1L, false))
            .thenReturn(Mono.just(new RoomDTO(1L, 1L, "101", "Suite",
                new BigDecimal("100.00"), 2, false, "Luxury suite")));
        when(motelServiceClient.getMotelById(1L)).thenReturn(Mono.just(motel));

        // Act & Assert
        StepVerifier.create(bookingService.createBooking(fiveNightRequest))
            .expectNextMatches(response ->
                response.totalPrice().compareTo(new BigDecimal("500.00")) == 0
            )
            .verifyComplete();
    }
}
