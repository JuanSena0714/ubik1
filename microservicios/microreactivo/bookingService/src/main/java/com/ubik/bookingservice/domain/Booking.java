package com.ubik.bookingservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("bookings")
public record Booking(
    @Id Long id,
    @Column("user_id") Long userId,
    @Column("room_id") Long roomId,
    @Column("motel_id") Long motelId,
    @Column("check_in_date") LocalDate checkInDate,
    @Column("check_out_date") LocalDate checkOutDate,
    @Column("total_price") BigDecimal totalPrice,
    @Column("status") String status, // PENDING, CONFIRMED, CANCELLED, COMPLETED
    @Column("guest_name") String guestName,
    @Column("guest_email") String guestEmail,
    @Column("guest_phone") String guestPhone,
    @Column("special_requests") String specialRequests,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt
) {}
