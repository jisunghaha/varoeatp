package com.example.demo.service;

import com.example.demo.domain.StoreTable;
import com.example.demo.domain.User;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StoreTableRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private StoreTableRepository storeTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long tableId;
    private String username = "testuser";

    @BeforeEach
    public void setUp() {
        reservationRepository.deleteAll();
        storeTableRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 테이블 생성 (1개의 물리적 테이블)
        StoreTable table = new StoreTable();
        table.setName("Test Table 1");
        // table.setTotalCount(5); // 제거됨
        table.setCapacityMin(1);
        table.setCapacityMax(4);
        storeTableRepository.save(table);
        tableId = table.getId();

        // 테스트용 유저 생성
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUsername(username + i);
            user.setEmail("test" + i + "@example.com");
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }
    }

    @Test
    public void concurrencyReservationTest() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    request.setTableId(tableId);
                    request.setDate(LocalDate.now().plusDays(1));
                    request.setTime("12:00");
                    request.setPartySize(2);

                    reservationService.createReservation(request, username + finalI);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 검증: 1개의 테이블이므로 1명만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }
}
