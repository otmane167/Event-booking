package com.event.booking.model.dto;

import com.event.booking.model.entity.event.EventStatus;

import java.time.LocalDateTime;

public record EventResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime date,
        String venue,
        Integer capacity,
        EventStatus status,
        Long organizerId,
        String organizerName
) {}