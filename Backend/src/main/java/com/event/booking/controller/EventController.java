package com.event.booking.controller;

import com.event.booking.model.dto.EventResponseDTO;
import com.event.booking.model.entity.event.Event;
import com.event.booking.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public EventResponseDTO create(@Valid @RequestBody Event event, @RequestParam Long organizerId) {
        Event saved = eventService.create(event, organizerId);
        return eventService.toDto(saved);
    }

    @PutMapping("/{id}")
    public EventResponseDTO update(@PathVariable Long id, @Valid @RequestBody Event event, @RequestParam Long organizerId) {
        return eventService.toDto(eventService.update(id, event, organizerId));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam Long organizerId) {
        eventService.delete(id, organizerId);
    }

    @GetMapping
    public List<EventResponseDTO> findAll() {
        return eventService.findAll()
                .stream()
                .map(eventService::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EventResponseDTO findById(@PathVariable Long id) {
        return eventService.toDto(eventService.findById(id));
    }
}
