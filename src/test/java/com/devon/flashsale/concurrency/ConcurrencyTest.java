package com.devon.flashsale.concurrency;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.repository.OrderRepository;
import com.devon.flashsale.repository.PaymentRepository;
import com.devon.flashsale.service.OrderService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long eventId;

    @BeforeEach
    void setup() {
    	
        Event event = new Event();
        event.setEventName("Mega Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(100);
        event.setStartTime(LocalDateTime.now().minusMinutes(10));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        eventRepository.save(event);
        eventId = event.getEventId();
    }
    
    @AfterEach
    void postTestSetup() {
    	eventRepository.deleteById(eventId);
    }

    @Test
    void shouldNotOversellSeatsUnderHighConcurrency() throws InterruptedException {
        int threads = 500;
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < threads; i++) {
            final int user = i;
            executor.submit(() -> {
                try {
                    orderService.createOrder(eventId, 1, "key-" + user);
                } catch (Exception ignored) {
                    // Some orders will fail due to out-of-stock or lock retries
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Event updatedEvent = eventRepository.findById(eventId).orElseThrow();

        int totalOrderedSeats = orderRepository.findAll().stream()
        		.filter(o -> o.getEvent().getEventId().equals(eventId))
                .mapToInt(o -> o.getQty())
                .sum();

        System.out.println("Remaining seats: " + updatedEvent.getRemainingSeats());
        System.out.println("Total ordered seats: " + totalOrderedSeats);

        assertTrue(updatedEvent.getRemainingSeats() >= 0);
        assertEquals(100, updatedEvent.getTotalSeats());
        assertTrue(totalOrderedSeats <= 100);
    }
}
