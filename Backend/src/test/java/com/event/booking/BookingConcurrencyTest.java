package com.event.booking;

import com.event.booking.model.entity.Booking.Booking;
import com.event.booking.model.entity.TicketType.TicketType;
import com.event.booking.model.entity.User.Role;
import com.event.booking.model.entity.User.User;
import com.event.booking.model.entity.event.Event;
import com.event.booking.repository.BookingRepository;
import com.event.booking.repository.EventRepository;
import com.event.booking.repository.TicketTypeRepository;
import com.event.booking.repository.UserRepository;
import com.event.booking.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired private BookingService bookingService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private TicketTypeRepository ticketTypeRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;
    private Long eventId;
    private Long ticketTypeId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setNom("Test");
        user.setPrenom("User");
        user.setEmail("concurrency-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(Role.ORGANIZER); // change if your enum value is named differently
        user = userRepository.saveAndFlush(user);
        userId = user.getId();

        Event event = new Event();
        event.setTitle("Concurrency Test Event");
        event.setDate(LocalDateTime.now().plusDays(30));
        event.setVenue("Test Venue");
        event.setCapacity(10);
        event.setOrganizer(user);
        event = eventRepository.saveAndFlush(event);
        eventId = event.getId();

        TicketType ticketType = new TicketType();
        ticketType.setName("VIP");
        ticketType.setPrice(100.0);
        ticketType.setQuantityAvailable(1);
        ticketType.setEvent(event);
        ticketType = ticketTypeRepository.saveAndFlush(ticketType);
        ticketTypeId = ticketType.getId();
    }

    @AfterEach
    void cleanUp() {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        bookingRepository.deleteAll(bookings);
        ticketTypeRepository.deleteById(ticketTypeId);
        eventRepository.deleteById(eventId);
        userRepository.deleteById(userId);
    }

    @Test
    void onlyOneBookingShouldSucceedWhenStockIsOne() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable bookingAttempt = () -> {
            try {
                ready.countDown();
                start.await(); // both threads fire at the same moment
                bookingService.create(userId, ticketTypeId, 1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                System.out.println("Booking failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        };

        executor.submit(bookingAttempt);
        executor.submit(bookingAttempt);

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one booking should succeed");
        assertEquals(1, failureCount.get(), "Exactly one booking should fail");

        int remainingStock = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow().getQuantityAvailable();
        assertEquals(0, remainingStock, "Stock should be exactly 0 after one successful booking");
    }
}