package com.ubik.bookingservice.dto;

import java.time.LocalDateTime;

/**
 * DTO para Motel que coincide con el MotelResponse del Motel Management Service
 * Los nombres de campos deben coincidir exactamente para la deserialización
 */
public record MotelDTO(
    Long id,
    String name,
    String address,
    String phoneNumber,      // Cambiado de phone para coincidir con MotelResponse
    String description,
    String city,
    Long propertyId,         // Agregado para coincidir con MotelResponse
    LocalDateTime dateCreated // Agregado para coincidir con MotelResponse
) {
    // Método helper para mantener compatibilidad con código existente
    public String phone() {
        return phoneNumber;
    }
}
