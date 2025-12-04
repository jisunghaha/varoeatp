package com.example.demo.repository;

// [중요] 아까 entity 폴더에서 찾았던 Store 파일을 import합니다.

import com.example.demo.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    java.util.Optional<Store> findByStoreName(String storeName);
}