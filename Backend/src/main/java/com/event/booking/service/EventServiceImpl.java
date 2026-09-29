package com.event.booking.service;

import com.event.booking.exception.ResourceNotFoundException;
import com.event.booking.model.dto.EventResponseDTO;
import com.event.booking.model.entity.event.Event;
import com.event.booking.model.entity.User.User;
import com.event.booking.repository.EventRepository;
import com.event.booking.repository.TicketTypeRepository;
import com.event.booking.repository.UserRepository;
import com.event.booking.model.entity.User.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public Event create(Event event, Long organizerId) {
        User organizer = requireOrganizer(organizerId);
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    @Override
    public Event update(Long id, Event event, Long organizerId) {
        Event existing = findById(id);
        verifyOwnership(existing, organizerId);
        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setDate(event.getDate());
        existing.setVenue(event.getVenue());
        existing.setCapacity(event.getCapacity());
        existing.setStatus(event.getStatus());
        return eventRepository.save(existing);
    }

    @Override
    public void delete(Long id, Long organizerId) {
        Event event = findById(id);
        verifyOwnership(event, organizerId);
        if (!ticketTypeRepository.findByEventId(id).isEmpty()) {
            throw new IllegalStateException("Delete this event's ticket types before deleting the event.");
        }
        eventRepository.delete(event);
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Override
    public EventResponseDTO toDto(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getVenue(),
                event.getCapacity(),
                event.getStatus(),
                event.getOrganizer().getId(),
                event.getOrganizer().getNom() + " " + event.getOrganizer().getPrenom()
        );
    }

    private User requireOrganizer(Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        if (organizer.getRole() != Role.ORGANIZER) {
            throw new AccessDeniedException("Only organizers can manage events.");
        }
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();
        if (authenticatedEmail == null || !organizer.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException("You can only manage events from your own account.");
        }
        return organizer;
    }

    private void verifyOwnership(Event event, Long organizerId) {
        requireOrganizer(organizerId);
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new AccessDeniedException("You can only manage your own events.");
        }
    }
}
