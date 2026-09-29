package com.event.booking.model.dto;

public record TicketTypeResponseDTO(
        Long id,
        String name,
        Double price,
        Integer quantityAvailable,
        Long eventId
) {}