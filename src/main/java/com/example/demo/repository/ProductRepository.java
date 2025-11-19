// src/main/java/com/example/demo/repository/ProductRepository.java

package com.example.demo.repository;

import com.example.demo.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 💡 JPQL을 사용하여 'category' 필드의 중복 없는(DISTINCT) 목록을 조회합니다.
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories(); 
    List<Product> findByStoreId(Long storeId);
}