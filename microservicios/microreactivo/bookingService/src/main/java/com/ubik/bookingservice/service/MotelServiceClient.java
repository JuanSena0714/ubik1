package com.ubik.bookingservice.service;

import com.ubik.bookingservice.dto.MotelDTO;
import com.ubik.bookingservice.dto.RoomDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MotelServiceClient {

    private final WebClient webClient;

    public MotelServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${services.motel-management.url}") String motelServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(motelServiceUrl).build();
    }

    public Mono<RoomDTO> getRoomById(Long roomId) {
        return webClient.get()
            .uri("/rooms/{id}", roomId)
            .retrieve()
            .bodyToMono(RoomDTO.class);
    }

    public Mono<MotelDTO> getMotelById(Long motelId) {
        return webClient.get()
            .uri("/motels/{id}", motelId)
            .retrieve()
            .bodyToMono(MotelDTO.class);
    }

    public Mono<RoomDTO> updateRoomAvailability(Long roomId, Boolean available) {
        return getRoomById(roomId)
            .flatMap(room -> {
                RoomDTO updatedRoom = new RoomDTO(
                    room.id(),
                    room.motelId(),
                    room.roomNumber(),
                    room.roomType(),
                    room.pricePerNight(),
                    room.capacity(),
                    available,
                    room.description()
                );
                return webClient.put()
                    .uri("/rooms/{id}", roomId)
                    .bodyValue(updatedRoom)
                    .retrieve()
                    .bodyToMono(RoomDTO.class);
            });
    }
}
