package com.ubik.usermanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper para convertir entre DTOs web y modelo de dominio Motel
 */
@Component
public class MotelDtoMapper {

    /**
     * Convierte CreateMotelRequest a Motel de dominio
     */
    public Motel toDomain(CreateMotelRequest request) {
        if (request == null) {
            return null;
        }
        return new Motel(
                null, // El ID se generará en la BD
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                request.propertyId(),
                LocalDateTime.now()
        );
    }

    /**
     * Convierte UpdateMotelRequest a Motel de dominio (sin ID ni fecha)
     */
    public Motel toDomain(UpdateMotelRequest request) {
        if (request == null) {
            return null;
        }
        return new Motel(
                null, // Se establecerá en el servicio
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                null, // Se mantendrá el existente
                null  // Se mantendrá la existente
        );
    }

    /**
     * Convierte Motel de dominio a MotelResponse
     */
    public MotelResponse toResponse(Motel motel) {
        if (motel == null) {
            return null;
        }
        return new MotelResponse(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated()
        );
    }
}