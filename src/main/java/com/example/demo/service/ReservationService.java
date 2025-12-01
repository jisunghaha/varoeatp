package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreTableRepository storeTableRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository; // 👇 [추가] 메뉴(상품) 조회를 위해 필요

    // 운영 시간 설정
    private static final LocalTime OPEN_TIME = LocalTime.of(11, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 날짜별 예약 가능 시간 조회
     */
    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date) {
        List<AvailableTimeResponse> times = new ArrayList<>();

        // 전체 테이블 수 계산
        int totalTableCapacity = storeTableRepository.findAll().stream()
                .mapToInt(StoreTable::getTotalCount).sum();
        if (totalTableCapacity == 0) totalTableCapacity = 1;

        // 해당 날짜의 예약 현황 조회
        Map<LocalTime, Long> reservationsByTime = reservationRepository.findByReservationDate(date).stream()
                .collect(Collectors.groupingBy(Reservation::getReservationTime, Collectors.counting()));

        // 시간대별 상태 확인
        LocalTime currentTime = OPEN_TIME;
        while (currentTime.isBefore(CLOSE_TIME)) {
            long reservedCount = reservationsByTime.getOrDefault(currentTime, 0L);
            String status;

            if (reservedCount >= totalTableCapacity) {
                status = "full";
            } else if (reservedCount > totalTableCapacity * 0.7) {
                status = "popular";
            } else {
                status = "available";
            }

            times.add(new AvailableTimeResponse(currentTime.format(TIME_FORMATTER), status));
            currentTime = currentTime.plusMinutes(30);
        }
        return times;
    }

    /**
     * 인원수와 시간에 맞는 테이블 옵션 조회
     */
    public List<TableOptionResponse> getAvailableTableOptions(LocalDate date, LocalTime time, int partySize) {
        // 인원수에 맞는 테이블 종류 검색
        List<StoreTable> matchingTables = storeTableRepository.findByCapacityMinLessThanEqualAndCapacityMaxGreaterThanEqual(partySize, partySize);

        List<TableOptionResponse> response = new ArrayList<>();
        for (StoreTable table : matchingTables) {
            // 해당 시간대 잔여 테이블 수 계산
            int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(date, time, table.getId());
            int availableCount = table.getTotalCount() - reservedCount;

            response.add(new TableOptionResponse(table, availableCount));
        }
        return response;
    }

    /**
     * 예약 생성 (메뉴 주문 포함)
     */
    @Transactional
    public Reservation createReservation(ReservationRequest request, String identifier) {
        // 1. 사용자 조회 (ID -> 이메일 -> 카카오ID 순)
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByUsername(identifier + "_kakao"))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + identifier));

        // 2. 테이블 조회
        StoreTable table = storeTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다."));

        LocalTime time = LocalTime.parse(request.getTime(), TIME_FORMATTER);

        // 3. 중복 예약(만석) 체크
        int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(request.getDate(), time, table.getId());
        if (reservedCount >= table.getTotalCount()) {
            throw new IllegalStateException("이미 마감된 테이블입니다. 다른 시간을 선택해주세요.");
        }

        // 4. 예약 정보 생성
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStoreTable(table);
        reservation.setReservationDate(request.getDate());
        reservation.setReservationTime(time);
        reservation.setPartySize(request.getPartySize());

        // 5. [핵심] 주문한 메뉴(Items) 저장 로직
        // 프론트에서 items 목록을 보냈다면 처리
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ReservationRequest.ReservationItemRequest itemReq : request.getItems()) {
                // 상품 정보 조회
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemReq.getProductId()));

                // 예약 상품 엔티티 생성 (연관관계 설정)
                ReservationItem reservationItem = new ReservationItem(reservation, product, itemReq.getCount());

                // 예약 객체에 추가 (Cascade 설정으로 인해 예약 저장 시 같이 저장됨)
                reservation.getItems().add(reservationItem);
            }
        }

        // 6. 예약 저장 (메뉴들도 함께 저장됨)
        return reservationRepository.save(reservation);
    }

    /**
     * 특정 사용자의 예약 내역 조회
     */
    public List<ReservationResponse> getReservationsByUser(String identifier) {
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByUsername(identifier + "_kakao"))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return reservationRepository.findByUserOrderByIdDesc(user).stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }
}