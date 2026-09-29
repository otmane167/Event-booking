package com.event.booking.repository;

import com.event.booking.model.entity.Booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByTicketTypeId(Long ticketTypeId);
}