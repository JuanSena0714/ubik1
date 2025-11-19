package com.ubik.bookingservice.dto;

import java.math.BigDecimal;

public record RoomDTO(
    Long id,
    Long motelId,
    String roomNumber,
    String roomType,
    BigDecimal pricePerNight,
    Integer capacity,
    Boolean available,
    String description
) {}
