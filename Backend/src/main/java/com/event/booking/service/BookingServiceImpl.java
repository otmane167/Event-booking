package com.event.booking.service;

import com.event.booking.exception.InsufficientStockException;
import com.event.booking.exception.ResourceNotFoundException;
import com.event.booking.model.dto.BookingResponseDTO;
import com.event.booking.model.entity.Booking.Booking;
import com.event.booking.model.entity.TicketType.TicketType;
import com.event.booking.model.entity.User.User;
import com.event.booking.repository.BookingRepository;
import com.event.booking.repository.TicketTypeRepository;
import com.event.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public Booking create(Long userId, Long ticketTypeId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType not found with id: " + ticketTypeId));

        if (ticketType.getQuantityAvailable() < quantity) {
            throw new InsufficientStockException(
                    "Not enough tickets available for ticket type: " + ticketType.getName());
        }

        ticketType.setQuantityAvailable(ticketType.getQuantityAvailable() - quantity);
        ticketTypeRepository.save(ticketType);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTicketType(ticketType);
        booking.setQuantity(quantity);
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public BookingResponseDTO toDto(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus(),
                booking.getQuantity(),
                booking.getBookedAt(),
                booking.getUser().getId(),
                booking.getTicketType().getId(),
                booking.getTicketType().getName()
        );
    }
}