package com.event.booking.service;

import com.event.booking.model.dto.TicketTypeResponseDTO;
import com.event.booking.model.entity.TicketType.TicketType;

import java.util.List;

public interface TicketTypeService {
    TicketType create(TicketType ticketType, Long eventId, Long organizerId);
    TicketType update(Long id, TicketType ticketType, Long organizerId);
    void delete(Long id, Long organizerId);
    List<TicketType> findByEventId(Long eventId);
    TicketTypeResponseDTO toDto(TicketType ticketType);
}
