package com.ubik.bookingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
    Long id,
    Long userId,
    Long roomId,
    Long motelId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    BigDecimal totalPrice,
    String status,
    String guestName,
    String guestEmail,
    String guestPhone,
    String specialRequests,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    // Información adicional del motel y habitación
    String motelName,
    String roomNumber,
    String roomType
) {}
