package com.example.demo.repository;

import com.example.demo.domain.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {
    
    // 인원수에 맞는 테이블 조회
    // 예: partySize=4 이면 min <= 4 AND max >= 4 인 테이블 검색
    List<StoreTable> findByCapacityMinLessThanEqualAndCapacityMaxGreaterThanEqual(int partySize, int partySizeCopy);
}