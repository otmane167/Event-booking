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
    public TicketTypeResponseDTO create(@Valid @RequestBody TicketType ticketType, @RequestParam Long eventId) {
        TicketType saved = ticketTypeService.create(ticketType, eventId);
        return ticketTypeService.toDto(saved);
    }

    @GetMapping
    public List<TicketTypeResponseDTO> findByEvent(@RequestParam Long eventId) {
        return ticketTypeService.findByEventId(eventId)
                .stream()
                .map(ticketTypeService::toDto)
                .collect(Collectors.toList());
    }
}