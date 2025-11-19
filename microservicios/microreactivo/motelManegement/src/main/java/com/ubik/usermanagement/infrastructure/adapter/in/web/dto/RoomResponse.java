package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

/**
 * DTO de respuesta para Room
 */
public record RoomResponse(
        Long id,
        Long motelId,
        String number,
        String roomType,
        Double price,
        String description,
        Boolean isAvailable
) {
}