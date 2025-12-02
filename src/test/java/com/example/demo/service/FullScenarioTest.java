package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.domain.Store;
import com.example.demo.domain.StoreTable;
import com.example.demo.domain.User;
import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.StoreTableRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class FullScenarioTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreTableRepository storeTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private User testUser;
    private Store testStore;
    private StoreTable testTable;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        testUser = new User();
        testUser.setEmail("scenario_test@example.com");
        testUser.setUsername("ScenarioUser");
        testUser.setPassword("password");
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        // 2. 매장 생성
        testStore = new Store("Test Store", "Busan", 35.0, 129.0, "010-1234-5678", true);
        storeRepository.save(testStore);

        // 3. 상품 생성
        Product product = new Product("Test Food", 10000, "Yummy", testStore.getId(), "Main", "img.jpg");
        productRepository.save(product);

        // 4. 테이블 생성 (개별 테이블)
        testTable = new StoreTable();
        testTable.setName("Window Seat 1");
        testTable.setCapacityMin(1);
        testTable.setCapacityMax(4);
        testTable.setStore(testStore);
        storeTableRepository.save(testTable);
    }

    @Test
    @DisplayName("전체 시나리오 테스트: 매장 확인 -> 예약 가능 시간 조회 -> 테이블 선택 -> 예약 -> 내역 확인")
    void fullReservationFlow() {
        LocalDate date = LocalDate.now().plusDays(2);
        String time = "18:00";

        // 1. 매장 및 상품 데이터 확인
        List<Store> stores = storeRepository.findAll();
        assertThat(stores).isNotEmpty();
        List<Product> products = productRepository.findByStoreId(testStore.getId());
        assertThat(products).isNotEmpty();

        // 2. 예약 가능 시간 조회
        List<AvailableTimeResponse> availableTimes = reservationService.getAvailableTimes(date);
        assertThat(availableTimes).isNotEmpty();

        // 3. 예약 가능한 테이블 조회
        List<TableOptionResponse> tableOptions = reservationService.getAvailableTableOptions(date, java.time.LocalTime.parse(time), 2);
        assertThat(tableOptions).isNotEmpty();
        Long targetTableId = tableOptions.get(0).getId();

        // 4. 예약 요청 생성
        ReservationRequest request = new ReservationRequest();
        // request.setStoreId(testStore.getId()); // 불필요
        request.setTableId(targetTableId);
        request.setDate(date);
        request.setTime(time);
        request.setPartySize(2);

        // 5. 예약 실행
        reservationService.createReservation(request, testUser.getEmail());

        // 6. 예약 내역 확인
        List<ReservationResponse> myReservations = reservationService.getReservationsByUser(testUser.getEmail());
        assertThat(myReservations).hasSize(1);
        assertThat(myReservations.get(0).getStoreName()).isEqualTo(testStore.getStoreName());
        assertThat(myReservations.get(0).getTableName()).isEqualTo(testTable.getName());
    }
}
