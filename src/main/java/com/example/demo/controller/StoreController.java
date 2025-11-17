package com.example.demo.controller;

import com.example.demo.domain.Store;
import com.example.demo.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @PostConstruct // 서버 시작 시 1회 자동 실행
    public void initTestStores() {
        
        if (storeRepository.count() == 0) {
            System.out.println("====== DB가 비어있어, 테스트 매장 3개 추가를 시작합니다... ======");
            try {
               
                Store testStore1 = new Store(
                    "수영국밥", 
                    "부산 부산진구 가야공원로 59 1,2층",
                     35.14795724176053, 129.03018703293802, // 새 좌표
                    "0507-1352-8297", true
                );
                Store testStore2 = new Store(
                    "세연정 가야점",
                    "부산 부산진구 가야대로 554",
                    35.15361309245611, 129.0326784417889 , // 새 좌표
                    "051-867-2000", true
                );
                Store testStore3 = new Store(
                    "타키온",
                    "부산 부산진구 대학로 76 1층",
                    35.149059214982096, 129.0344660298509, // 새 좌표
                    "051-891-1009", true
                );

                storeRepository.save(testStore1);
                storeRepository.save(testStore2);
                storeRepository.save(testStore3);
                
                System.out.println("====== 테스트 매장 3개 DB에 자동 추가 완료 ======");

            } catch (Exception e) {
                System.out.println("====== [오류] 테스트 매장 추가 중 실패: " + e.getMessage() + " ======");
            }
        } else {
            System.out.println("====== DB에 이미 데이터가 있으므로, 테스트 매장 추가를 건너뜁니다. ======");
        }
    }

    // 매장 목록 전체 조회 (지도 표시용)
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        List<Store> stores = storeRepository.findAll(); 
        return ResponseEntity.ok(stores);
    }
    
    // 매장 1개 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID " + id + "에 해당하는 매장을 찾을 수 없습니다."));
        return ResponseEntity.ok(store);
    }
    
    @GetMapping("/add-test-store")
    public String addTestStore() {
        return "<h1>이 기능은 이제 서버 시작 시 @PostConstruct로 자동 실행됩니다.</h1>";
    }
}