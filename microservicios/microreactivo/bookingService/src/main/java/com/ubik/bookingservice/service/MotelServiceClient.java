package com.ubik.bookingservice.service;

import com.ubik.bookingservice.config.HeaderPropagationWebClientFilter;
import com.ubik.bookingservice.dto.MotelDTO;
import com.ubik.bookingservice.dto.RoomDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for communicating with the Motel Management Service through the API Gateway.
 * Propagates user authentication headers to maintain security context.
 */
@Service
public class MotelServiceClient {

    private final WebClient webClient;

    public MotelServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${services.motel-management.url}") String motelServiceUrl,
                             HeaderPropagationWebClientFilter headerPropagationFilter) {
        this.webClient = webClientBuilder
            .baseUrl(motelServiceUrl)
            .filter(headerPropagationFilter)
            .build();
    }

    public Mono<RoomDTO> getRoomById(Long roomId) {
        return webClient.get()
            .uri("/api/rooms/{id}", roomId)
            .retrieve()
            .bodyToMono(RoomDTO.class);
    }

    public Mono<MotelDTO> getMotelById(Long motelId) {
        return webClient.get()
            .uri("/api/motels/{id}", motelId)
            .retrieve()
            .bodyToMono(MotelDTO.class);
    }

    public Mono<RoomDTO> updateRoomAvailability(Long roomId, Boolean available) {
        return getRoomById(roomId)
            .flatMap(room -> {
                // Crear DTO actualizado con los nombres de campo correctos
                RoomDTO updatedRoom = new RoomDTO(
                    room.id(),
                    room.motelId(),
                    room.number(),          // Usar number en lugar de roomNumber()
                    room.roomType(),
                    room.price(),           // Usar price() directamente
                    room.description(),
                    available               // Nueva disponibilidad
                );
                return webClient.put()
                    .uri("/api/rooms/{id}", roomId)
                    .bodyValue(updatedRoom)
                    .retrieve()
                    .bodyToMono(RoomDTO.class);
            });
    }
}
