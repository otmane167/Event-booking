package com.event.booking.controller;

import com.event.booking.model.dto.TicketTypeResponseDTO;
import com.event.booking.model.entity.TicketType.TicketType;
import com.event.booking.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    public TicketTypeResponseDTO create(@Valid @RequestBody TicketType ticketType, @RequestParam Long eventId, @RequestParam Long organizerId) {
        TicketType saved = ticketTypeService.create(ticketType, eventId, organizerId);
        return ticketTypeService.toDto(saved);
    }

    @PutMapping("/{id}")
    public TicketTypeResponseDTO update(@PathVariable Long id, @Valid @RequestBody TicketType ticketType, @RequestParam Long organizerId) {
        return ticketTypeService.toDto(ticketTypeService.update(id, ticketType, organizerId));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam Long organizerId) {
        ticketTypeService.delete(id, organizerId);
    }

    @GetMapping
    public List<TicketTypeResponseDTO> findByEvent(@RequestParam Long eventId) {
        return ticketTypeService.findByEventId(eventId)
                .stream()
                .map(ticketTypeService::toDto)
                .collect(Collectors.toList());
    }
}
