package com.ubik.bookingservice.dto;

public record MotelDTO(
    Long id,
    String name,
    String address,
    String city,
    String phone,
    Double rating,
    String description
) {}
