package com.event.booking.controller;

import com.event.booking.model.dto.BookingResponseDTO;
import com.event.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDTO create(@RequestParam Long userId, @RequestParam Long ticketTypeId, @RequestParam Integer quantity) {
        var booking = bookingService.create(userId, ticketTypeId, quantity);
        return bookingService.toDto(booking);
    }

    @GetMapping
    public List<BookingResponseDTO> findByUser(@RequestParam Long userId) {
        return bookingService.findByUserId(userId)
                .stream()
                .map(bookingService::toDto)
                .collect(Collectors.toList());
    }
}