package com.example.demo.service;

import com.example.demo.domain.Reservation;
import com.example.demo.domain.StoreTable;
import com.example.demo.domain.User;
import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StoreTableRepository;
import com.example.demo.repository.UserRepository;
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
    private final UserRepository userRepository; // 기존 UserRepository 사용

    // 운영 시간 (예: 오전 11시 ~ 오후 9시, 30분 간격)
    private static final LocalTime OPEN_TIME = LocalTime.of(11, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date) {
        List<AvailableTimeResponse> times = new ArrayList<>();
        
        // 전체 테이블 수 (모든 종류의 테이블 개수 총합)
        int totalTableCapacity = storeTableRepository.findAll().stream()
                                    .mapToInt(StoreTable::getTotalCount).sum();
        if (totalTableCapacity == 0) totalTableCapacity = 1; // 테이블이 0개일 때 오류 방지

        // 해당 날짜의 모든 예약
        Map<LocalTime, Long> reservationsByTime = reservationRepository.findByReservationDate(date).stream()
                .collect(Collectors.groupingBy(Reservation::getReservationTime, Collectors.counting()));

        LocalTime currentTime = OPEN_TIME;
        while (currentTime.isBefore(CLOSE_TIME)) {
            long reservedCount = reservationsByTime.getOrDefault(currentTime, 0L);
            String status;

            if (reservedCount >= totalTableCapacity) {
                status = "full"; // 마감
            } else if (reservedCount > totalTableCapacity * 0.7) {
                status = "popular"; // 인기
            } else {
                status = "available"; // 가능
            }

            times.add(new AvailableTimeResponse(currentTime.format(TIME_FORMATTER), status));
            currentTime = currentTime.plusMinutes(30);
        }
        return times;
    }

    public List<TableOptionResponse> getAvailableTableOptions(LocalDate date, LocalTime time, int partySize) {
        // 1. 인원수에 맞는 테이블 찾기
        List<StoreTable> matchingTables = storeTableRepository.findByCapacityMinLessThanEqualAndCapacityMaxGreaterThanEqual(partySize, partySize);

        List<TableOptionResponse> response = new ArrayList<>();
        for (StoreTable table : matchingTables) {
            // 2. 해당 날짜/시간에 이 테이블이 몇 개 예약되었는지 확인
            int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(date, time, table.getId());
            
            // 3. 남은 테이블 수 계산
            int availableCount = table.getTotalCount() - reservedCount;
            
            response.add(new TableOptionResponse(table, availableCount));
        }
        return response;
    }

    @Transactional
    public Reservation createReservation(ReservationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
                if (user == null) {
    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
}
        
        StoreTable table = storeTableRepository.findById(request.getTableId())
                .orElse(null);
if (table == null) {
    throw new IllegalArgumentException("테이블을 찾을 수 없습니다.");
}
        LocalTime time = LocalTime.parse(request.getTime(), TIME_FORMATTER);

        // (중요) 예약 가능 여부 최종 확인 (동시성 문제 고려 필요)
        int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(request.getDate(), time, table.getId());
        if (reservedCount >= table.getTotalCount()) {
            throw new IllegalStateException("이미 마감된 테이블입니다. 다른 시간을 선택해주세요.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStoreTable(table);
        reservation.setReservationDate(request.getDate());
        reservation.setReservationTime(time);
        reservation.setPartySize(request.getPartySize());

        return reservationRepository.save(reservation);
    }
}