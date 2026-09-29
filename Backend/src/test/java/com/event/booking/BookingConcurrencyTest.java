package com.event.booking;

import com.event.booking.repository.TicketTypeRepository;
import com.event.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Test
    void onlyOneBookingShouldSucceedWhenStockIsOne() throws InterruptedException {
        Long ticketTypeId = 2L; // the VIP ticket type with quantityAvailable = 1
        Long userId = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable bookingAttempt = () -> {
            try {
                bookingService.create(userId, ticketTypeId, 1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                System.out.println("Booking failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        };

        Future<?> f1 = executor.submit(bookingAttempt);
        Future<?> f2 = executor.submit(bookingAttempt);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Successes: " + successCount.get());
        System.out.println("Failures: " + failureCount.get());

        assertEquals(1, successCount.get(), "Exactly one booking should succeed");
        assertEquals(1, failureCount.get(), "Exactly one booking should fail");

        int remainingStock = ticketTypeRepository.findById(ticketTypeId).orElseThrow().getQuantityAvailable();
        assertEquals(0, remainingStock, "Stock should be exactly 0 after one successful booking");
    }
}