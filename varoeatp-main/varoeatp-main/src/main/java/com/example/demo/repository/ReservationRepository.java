package com.example.demo.repository;

import com.example.demo.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // 특정 날짜와 시간에 예약된 특정 테이블의 수 계산
    int countByReservationDateAndReservationTimeAndStoreTable_Id(LocalDate date, LocalTime time, Long tableId);

    // 특정 날짜의 모든 예약 조회
    List<Reservation> findByReservationDate(LocalDate date);
    List<Reservation> findAllByUser(User user);
}