package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Motel
 */
public record MotelResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String description,
        String city,
        Long propertyId,
        LocalDateTime dateCreated
) {
}