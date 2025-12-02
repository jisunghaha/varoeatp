package com.example.demo.repository;

import com.example.demo.domain.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {

    List<StoreTable> findByStoreId(Long storeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM StoreTable t WHERE t.id = :id")
    Optional<StoreTable> findByIdWithLock(@Param("id") Long id);
}