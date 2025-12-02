package com.example.demo.service;

import com.example.demo.domain.Store;
import com.example.demo.domain.StoreTable;
import com.example.demo.domain.User;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.StoreTableRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test") // Use a test profile if available, or default
public class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private StoreTableRepository storeTableRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Long tableId;
    private String userIdentifier = "testuser@example.com";

    @BeforeEach
    public void setUp() {
        // Clean up
        reservationRepository.deleteAll();
        storeTableRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();

        // Create User
        User user = new User();
        user.setEmail(userIdentifier);
        user.setUsername("Test User");
        user.setNickname("Tester");
        user.setPassword("password");
        user.setPhoneNumber("010-1234-5678");
        user.setRole("USER");
        user.setProvider("local");
        userRepository.save(user);

        // Create Store
        Store store = new Store();
        store.setStoreName("Test Store");
        store.setAddress("Seoul");
        store.setPhoneNumber("02-123-4567");
        storeRepository.save(store);

        // Create Table with capacity 5 (totalCount represents concurrent capacity here)
        StoreTable table = new StoreTable();
        table.setStore(store);
        table.setName("Table 1");
        table.setTotalCount(5); // Only 5 reservations allowed
        table.setCapacity(4);
        table.setCapacityMax(4);
        table.setCapacityMin(2);
        storeTableRepository.save(table);
        tableId = table.getId();
    }

    @Test
    @DisplayName("Concurrency Test: 20 requests for 5 seats")
    public void testConcurrency() throws InterruptedException {
        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ReservationRequest request = new ReservationRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setTime("12:00");
        request.setPartySize(2);
        request.setTableId(tableId);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.createReservation(request, userIdentifier);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("Success: " + successCount.get());
        System.out.println("Fail: " + failCount.get());

        // Without locking, this might fail (success > 5)
        // With locking, this must be exactly 5
        assertEquals(5, successCount.get(), "Only 5 reservations should succeed");
    }
}
