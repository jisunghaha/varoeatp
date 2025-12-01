package com.example.demo.repository;

import com.example.demo.domain.Reservation;
import com.example.demo.domain.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
    // 특정 예약 목록에 포함된 모든 상품(메뉴)을 삭제하는 명령어
    void deleteByReservationIn(List<Reservation> reservations);
}