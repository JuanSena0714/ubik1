package com.ubik.bookingservice.dto;

import java.math.BigDecimal;

/**
 * DTO para Room que coincide con el RoomResponse del Motel Management Service
 * Los nombres de campos deben coincidir exactamente para la deserialización
 */
public record RoomDTO(
    Long id,
    Long motelId,
    String number,           // Cambiado de roomNumber para coincidir con RoomResponse
    String roomType,
    Double price,            // Cambiado de BigDecimal pricePerNight a Double price para coincidir con RoomResponse
    String description,
    Boolean isAvailable      // Cambiado de available para coincidir con RoomResponse
) {
    // Método helper para obtener el precio como BigDecimal para cálculos
    public BigDecimal pricePerNight() {
        return price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO;
    }

    // Método helper para verificar disponibilidad con nombre más legible
    public Boolean available() {
        return isAvailable;
    }

    // Método helper para obtener el número de habitación con nombre más legible
    public String roomNumber() {
        return number;
    }
}
