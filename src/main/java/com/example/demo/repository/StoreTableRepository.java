package com.example.demo.repository;

import com.example.demo.domain.StoreTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {
    
    // 인원수에 맞는 테이블 조회
    // 예: partySize=4 이면 min <= 4 AND max >= 4 인 테이블 검색
    List<StoreTable> findByCapacityMinLessThanEqualAndCapacityMaxGreaterThanEqual(int partySize, int partySizeCopy);

    /**
     * ID로 테이블을 조회하면서 비관적 락(Pessimistic Lock)을 획득합니다.
     * <p>
     * 이 메서드는 트랜잭션이 끝날 때까지 해당 테이블 행(Row)에 락을 겁니다.
     * 다른 트랜잭션은 이 락이 해제될 때까지 해당 데이터를 읽거나 수정할 수 없습니다.
     * 동시성 제어가 필요한 예약 생성 등의 로직에서 사용하세요.
     * </p>
     *
     * @param id 조회할 테이블의 ID
     * @return 락이 걸린 테이블 엔티티 (Optional)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StoreTable s where s.id = :id")
    Optional<StoreTable> findByIdWithLock(@Param("id") Long id);
}