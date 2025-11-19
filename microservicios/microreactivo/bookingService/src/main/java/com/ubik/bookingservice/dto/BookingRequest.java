package com.ubik.bookingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;

public record BookingRequest(
    @NotNull Long userId,
    @NotNull Long roomId,
    @NotNull Long motelId,
    @NotNull @Future LocalDate checkInDate,
    @NotNull @Future LocalDate checkOutDate,
    @NotNull String guestName,
    @NotNull @Email String guestEmail,
    @NotNull String guestPhone,
    String specialRequests
) {}
