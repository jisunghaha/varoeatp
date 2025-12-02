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
import com.example.demo.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    // 운영 시간 (예: 오전 11시 ~ 오후 9시, 30분 간격)
    private static final LocalTime OPEN_TIME = LocalTime.of(11, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date) {
        List<AvailableTimeResponse> times = new ArrayList<>();

        // 전체 테이블 수 계산
        int totalTableCapacity = storeTableRepository.findAll().stream()
                .mapToInt(StoreTable::getTotalCount).sum();
        if (totalTableCapacity == 0)
            totalTableCapacity = 1;

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

    public List<TableOptionResponse> getAvailableTableOptions(Long storeId, LocalDate date, LocalTime time,
            int partySize) {
        // 1. 매장 및 인원수에 맞는 테이블 찾기
        List<StoreTable> matchingTables = storeTableRepository.findByStoreId(storeId).stream()
                .filter(table -> table.getCapacityMin() <= partySize && table.getCapacityMax() >= partySize)
                .collect(Collectors.toList());

        List<TableOptionResponse> response = new ArrayList<>();
        for (StoreTable table : matchingTables) {
            // 해당 시간대 잔여 테이블 수 계산
            int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(date,
                    time, table.getId());
            int availableCount = table.getTotalCount() - reservedCount;

            response.add(new TableOptionResponse(table, availableCount));
        }
        return response;
    }

    @Transactional
    public Reservation createReservation(ReservationRequest request, String userIdentifier) {
        // 이메일 또는 사용자 이름으로 사용자 찾기
        User user = userRepository.findByEmail(userIdentifier)
                .or(() -> userRepository.findByUsername(userIdentifier))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userIdentifier));

        if (request.getTableId() == null) {
            throw new IllegalArgumentException("테이블 ID는 필수입니다.");
        }

        // 2. 테이블 조회 (비관적 락 적용)
        StoreTable table = storeTableRepository.findByIdWithLock(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다."));

        LocalTime time = LocalTime.parse(request.getTime(), TIME_FORMATTER);

        // 3. 중복 예약(만석) 체크
        int reservedCount = reservationRepository
                .countByReservationDateAndReservationTimeAndStoreTable_Id(request.getDate(), time, table.getId());

        if (reservedCount >= table.getTotalCount()) {
            throw new IllegalStateException("해당 시간대의 테이블이 모두 예약되었습니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStoreTable(table);
        reservation.setStore(table.getStore());
        reservation.setReservationDate(request.getDate());
        reservation.setReservationTime(time);
        reservation.setPartySize(request.getPartySize());
        reservation.setNumOfPeople(request.getPartySize());

        // Payment Info
        reservation.setPaymentMethod(request.getPaymentMethod());
        reservation.setImpUid(request.getImpUid());

        if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            reservation.setPaymentTime(java.time.LocalDateTime.now());
            if (request.getImpUid() != null && !request.getImpUid().isEmpty()) {
                reservation.setPaymentStatus("PAID");
            } else {
                reservation.setPaymentStatus("PENDING");
            }
        } else {
            reservation.setPaymentStatus("PENDING");
        }

        // Menu processing
        if (request.getMenus() != null && !request.getMenus().isEmpty()) {
            for (ReservationRequest.MenuRequest menuReq : request.getMenus()) {
                if (menuReq.getQuantity() > 0) {
                    com.example.demo.domain.Product product = productRepository.findById(menuReq.getProductId())
                            .orElseThrow(
                                    () -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + menuReq.getProductId()));
                    com.example.demo.domain.ReservationMenu reservationMenu = new com.example.demo.domain.ReservationMenu(
                            reservation, product, menuReq.getQuantity());
                    reservation.addReservationMenu(reservationMenu);
                }
            }
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId, String userIdentifier) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!isOwner(reservation.getUser(), userIdentifier)) {
            throw new IllegalStateException("예약 취소 권한이 없습니다.");
        }

        reservationRepository.delete(reservation);
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, ReservationRequest request, String userIdentifier) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!isOwner(reservation.getUser(), userIdentifier)) {
            throw new IllegalStateException("예약 변경 권한이 없습니다.");
        }

        LocalTime newTime = LocalTime.parse(request.getTime(), TIME_FORMATTER);
        LocalDate newDate = request.getDate();
        int newPartySize = request.getPartySize();

        StoreTable currentTable = reservation.getStoreTable();
        StoreTable targetTable = currentTable;

        // 1. 테이블 변경 필요 여부 확인
        boolean needNewTable = (request.getTableId() != null && !request.getTableId().equals(currentTable.getId())) ||
                (newPartySize < currentTable.getCapacityMin() || newPartySize > currentTable.getCapacityMax());

        if (needNewTable) {
            if (request.getTableId() != null) {
                targetTable = storeTableRepository.findByIdWithLock(request.getTableId())
                        .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다."));
            } else {
                List<StoreTable> candidates = storeTableRepository.findByStoreId(reservation.getStore().getId())
                        .stream()
                        .filter(t -> t.getCapacityMin() <= newPartySize && t.getCapacityMax() >= newPartySize)
                        .collect(Collectors.toList());

                StoreTable suitableTable = null;
                for (StoreTable candidate : candidates) {
                    int reservedCount = reservationRepository.countByReservationDateAndReservationTimeAndStoreTable_Id(
                            newDate, newTime, candidate.getId());
                    if (reservedCount < candidate.getTotalCount()) {
                        suitableTable = candidate;
                        break;
                    }
                }

                if (suitableTable == null) {
                    throw new IllegalStateException("해당 인원(" + newPartySize + "명)을 수용할 수 있는 빈 테이블이 없습니다.");
                }

                targetTable = storeTableRepository.findByIdWithLock(suitableTable.getId())
                        .orElseThrow(() -> new IllegalStateException("테이블 조회 실패"));
            }
        } else {
            targetTable = storeTableRepository.findByIdWithLock(targetTable.getId())
                    .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다."));
        }

        // 2. 가용성 체크
        boolean isSameSlot = reservation.getReservationDate().equals(newDate) &&
                reservation.getReservationTime().equals(newTime) &&
                reservation.getStoreTable().getId().equals(targetTable.getId());

        if (!isSameSlot) {
            int reservedCount = reservationRepository
                    .countByReservationDateAndReservationTimeAndStoreTable_Id(newDate, newTime, targetTable.getId());

            if (reservedCount >= targetTable.getTotalCount()) {
                throw new IllegalStateException("해당 시간대의 테이블이 모두 예약되었습니다.");
            }
        }

        // 3. 인원수 최종 체크
        if (newPartySize < targetTable.getCapacityMin() || newPartySize > targetTable.getCapacityMax()) {
            throw new IllegalArgumentException(
                    "선택한 테이블은 " + targetTable.getCapacityMin() + "~" + targetTable.getCapacityMax() + "명만 예약 가능합니다.");
        }

        // 4. 업데이트
        reservation.setStoreTable(targetTable);
        reservation.setStore(targetTable.getStore());
        reservation.setReservationDate(newDate);
        reservation.setReservationTime(newTime);
        reservation.setPartySize(newPartySize);
        reservation.setNumOfPeople(newPartySize);

        // Menu update
        if (request.getMenus() != null) {
            reservation.getReservationMenus().clear();
            for (ReservationRequest.MenuRequest menuReq : request.getMenus()) {
                if (menuReq.getQuantity() > 0) {
                    com.example.demo.domain.Product product = productRepository.findById(menuReq.getProductId())
                            .orElseThrow(
                                    () -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + menuReq.getProductId()));
                    com.example.demo.domain.ReservationMenu reservationMenu = new com.example.demo.domain.ReservationMenu(
                            reservation, product, menuReq.getQuantity());
                    reservation.addReservationMenu(reservationMenu);
                }
            }
        }

        return reservation;
    }

    private boolean isOwner(User user, String userIdentifier) {
        if (user.getEmail().equals(userIdentifier))
            return true;
        if (user.getUsername().equals(userIdentifier))
            return true;
        return false;
    }

    public List<com.example.demo.dto.ReservationResponse> getReservationsByUser(String userIdentifier) {
        User user = userRepository.findByEmail(userIdentifier)
                .or(() -> userRepository.findByUsername(userIdentifier))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userIdentifier));

        return reservationRepository.findByUserOrderByIdDesc(user).stream()
                .map(com.example.demo.dto.ReservationResponse::new)
                .collect(Collectors.toList());
    }

    public List<StoreTable> getTablesByStore(Long storeId) {
        return storeTableRepository.findByStoreId(storeId);
    }
}