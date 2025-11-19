package com.ubik.usermanagement.domain.model;

/**
 * Modelo de dominio para Room (Habitación)
 * Representa la entidad de negocio independiente de la infraestructura
 */
public record Room(
        Long id,
        Long motelId,
        String number,
        String roomType,
        Double price,
        String description,
        Boolean isAvailable
) {
    // Constructor para creación de nuevas habitaciones (sin ID)
    public static Room createNew(
            Long motelId,
            String number,
            String roomType,
            Double price,
            String description
    ) {
        return new Room(null, motelId, number, roomType, price, description, true);
    }

    // Constructor para actualización
    public Room withUpdatedInfo(
            String number,
            String roomType,
            Double price,
            String description,
            Boolean isAvailable
    ) {
        return new Room(this.id, this.motelId, number, roomType, price, description, isAvailable);
    }
}