package com.event.booking.service;

import com.event.booking.exception.ResourceNotFoundException;
import com.event.booking.model.dto.TicketTypeResponseDTO;
import com.event.booking.model.entity.event.Event;
import com.event.booking.model.entity.TicketType.TicketType;
import com.event.booking.repository.EventRepository;
import com.event.booking.repository.TicketTypeRepository;
import com.event.booking.repository.BookingRepository;
import com.event.booking.repository.UserRepository;
import com.event.booking.model.entity.User.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public TicketType create(TicketType ticketType, Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        verifyOwnership(event, organizerId);
        ticketType.setEvent(event);
        return ticketTypeRepository.save(ticketType);
    }

    @Override
    public TicketType update(Long id, TicketType ticketType, Long organizerId) {
        TicketType existing = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + id));
        verifyOwnership(existing.getEvent(), organizerId);
        existing.setName(ticketType.getName());
        existing.setPrice(ticketType.getPrice());
        existing.setQuantityAvailable(ticketType.getQuantityAvailable());
        return ticketTypeRepository.save(existing);
    }

    @Override
    public void delete(Long id, Long organizerId) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + id));
        verifyOwnership(ticketType.getEvent(), organizerId);
        if (!bookingRepository.findByTicketTypeId(id).isEmpty()) {
            throw new IllegalStateException("This ticket type has bookings and cannot be deleted.");
        }
        ticketTypeRepository.delete(ticketType);
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

    private void verifyOwnership(Event event, Long organizerId) {
        var organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();
        if (organizer.getRole() != Role.ORGANIZER || !event.getOrganizer().getId().equals(organizerId)
                || authenticatedEmail == null || !organizer.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException("You can only manage ticket types for your own events.");
        }
    }
}
