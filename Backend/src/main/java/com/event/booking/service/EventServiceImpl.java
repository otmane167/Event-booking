package com.event.booking.service;

import com.event.booking.exception.ResourceNotFoundException;
import com.event.booking.model.dto.EventResponseDTO;
import com.event.booking.model.entity.event.Event;
import com.event.booking.model.entity.User.User;
import com.event.booking.repository.EventRepository;
import com.event.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public Event create(Event event, Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
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
}