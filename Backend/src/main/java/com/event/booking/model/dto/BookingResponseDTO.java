package com.event.booking.model.dto;

import com.event.booking.model.entity.Booking.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponseDTO(
        Long id,
        BookingStatus status,
        Integer quantity,
        LocalDateTime bookedAt,
        Long userId,
        Long ticketTypeId,
        String ticketTypeName
) {}