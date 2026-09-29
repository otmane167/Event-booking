package com.event.booking.service;

import com.event.booking.model.dto.EventResponseDTO;
import com.event.booking.model.entity.event.Event;

import java.util.List;

public interface EventService {
    Event create(Event event, Long organizerId);
    Event update(Long id, Event event, Long organizerId);
    void delete(Long id, Long organizerId);
    List<Event> findAll();
    Event findById(Long id);
    EventResponseDTO toDto(Event event);
}
