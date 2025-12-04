package com.example.demo.repository;

import com.example.demo.domain.ReservationMenu;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationMenuRepository extends JpaRepository<ReservationMenu, Long> {

    @Modifying
    @Query("DELETE FROM ReservationMenu rm WHERE rm.reservation.user = :user")
    void deleteByReservationUser(@Param("user") User user);
}
