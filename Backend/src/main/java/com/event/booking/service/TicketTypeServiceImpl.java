package com.event.booking.service;

import com.event.booking.exception.ResourceNotFoundException;
import com.event.booking.model.dto.TicketTypeResponseDTO;
import com.event.booking.model.entity.event.Event;
import com.event.booking.model.entity.TicketType.TicketType;
import com.event.booking.repository.EventRepository;
import com.event.booking.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Override
    public TicketType create(TicketType ticketType, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        ticketType.setEvent(event);
        return ticketTypeRepository.save(ticketType);
    }

    @Override
    public List<TicketType> findByEventId(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId);
    }

    @Override
    public TicketTypeResponseDTO toDto(TicketType ticketType) {
        return new TicketTypeResponseDTO(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getQuantityAvailable(),
                ticketType.getEvent().getId()
        );
    }
}