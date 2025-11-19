package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.port.in.MotelUseCasePort;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Motel
 * Contiene la lógica de negocio
 */
@Service
public class MotelService implements MotelUseCasePort {

    private final MotelRepositoryPort motelRepositoryPort;

    public MotelService(MotelRepositoryPort motelRepositoryPort) {
        this.motelRepositoryPort = motelRepositoryPort;
    }

    @Override
    public Mono<Motel> createMotel(Motel motel) {
        // Validaciones de negocio
        return validateMotel(motel)
                .then(motelRepositoryPort.save(motel));
    }

    @Override
    public Mono<Motel> getMotelById(Long id) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)));
    }

    @Override
    public Flux<Motel> getAllMotels() {
        return motelRepositoryPort.findAll();
    }

    @Override
    public Flux<Motel> getMotelsByCity(String city) {
        return motelRepositoryPort.findByCity(city);
    }

    @Override
    public Mono<Motel> updateMotel(Long id, Motel motel) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)))
                .flatMap(existingMotel -> {
                    Motel updatedMotel = new Motel(
                            id,
                            motel.name(),
                            motel.address(),
                            motel.phoneNumber(),
                            motel.description(),
                            motel.city(),
                            existingMotel.propertyId(),
                            existingMotel.dateCreated()
                    );
                    return validateMotel(updatedMotel)
                            .then(motelRepositoryPort.update(updatedMotel));
                });
    }

    @Override
    public Mono<Void> deleteMotel(Long id) {
        return motelRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Motel no encontrado con ID: " + id));
                    }
                    return motelRepositoryPort.deleteById(id);
                });
    }

    /**
     * Validaciones de negocio para un motel
     */
    private Mono<Void> validateMotel(Motel motel) {
        if (motel.name() == null || motel.name().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El nombre del motel es requerido"));
        }
        if (motel.address() == null || motel.address().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La dirección del motel es requerida"));
        }
        if (motel.city() == null || motel.city().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La ciudad del motel es requerida"));
        }
        return Mono.empty();
    }
}