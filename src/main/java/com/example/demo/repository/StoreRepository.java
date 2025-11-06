package com.example.demo.repository;

// [중요] 아까 entity 폴더에서 찾았던 Store 파일을 import합니다.
import com.example.demo.domain.Store; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    // (지금은 이 안에 아무것도 적지 않아도 findAll() 기능은 작동합니다.)
}