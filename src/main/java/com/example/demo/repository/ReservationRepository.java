package com.example.demo.repository;

import com.example.demo.domain.Reservation;
import com.example.demo.domain.User; // User import 필요
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByReservationDateAndReservationTimeAndStoreTable_Id(LocalDate date, LocalTime time, Long tableId);
    List<Reservation> findByReservationDate(LocalDate date);

    List<Reservation> findByUserOrderByIdDesc(User user);
}