package com.event.booking.service;

import com.event.booking.model.dto.BookingResponseDTO;
import com.event.booking.model.entity.Booking.Booking;

import java.util.List;

public interface BookingService {
    Booking create(Long userId, Long ticketTypeId, Integer quantity);
    List<Booking> findByUserId(Long userId);
    BookingResponseDTO toDto(Booking booking);
}