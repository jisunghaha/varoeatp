package com.example.demo.controller;

// 👇 [확인 필요] 이 경로가 맞는지 확인하세요. (Store.java 파일이 있는 곳)
import com.example.demo.domain.Store; // 
// 👇 [확인 필요] 이 경로가 맞는지 확인하세요. (StoreRepository.java 파일이 있는 곳)
import com.example.demo.repository.StoreRepository; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores") // 이 API의 기본 주소
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    // '/api/stores' (GET 요청)가 오면 모든 매장을 DB에서 찾아 반환
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        // 친구의 DB에서 모든 매장 정보를 가져옵니다.
        List<Store> stores = storeRepository.findAll(); 
        
        // 매장 목록을 JSON 형태로 반환합니다.
        return ResponseEntity.ok(stores);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        // DB에서 ID로 Store를 찾음 (없으면 null 대신 오류 발생)
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID " + id + "에 해당하는 매장을 찾을 수 없습니다."));
        
        // 찾은 매장 1개를 JSON 형태로 반환
        return ResponseEntity.ok(store);
    }
    // (B) 임시로 매장을 3개 추가하는 API
    @GetMapping("/add-test-store")
    public String addTestStore() {
        try {
            // (1) 수영국밥
            Store testStore1 = new Store(
                "수영국밥", 
                "부산 부산진구 가야공원로 59 1,2층",
                35.14545,  // 위도 (Lat)
                129.03458,  // 경도 (Lng)
                "0507-1352-8297", // 전화번호
                true // 영업 여부
            );

            // (2) 세연정 가야점
            Store testStore2 = new Store(
                "세연정 가야점",
                "부산 부산진구 가야대로 554",
                35.15042,
                129.04018,
                "051-867-2000",
                true
            );

            // (3) 타키온
            Store testStore3 = new Store(
                "타키온",
                "부산 부산진구 대학로 76 1층",
                35.14815,
                129.04574,
                "051-891-1009",
                true
            );

            // DB에 3개 매장 저장
            storeRepository.save(testStore1);
            storeRepository.save(testStore2);
            storeRepository.save(testStore3);

            return "<h1>테스트 매장 3개 추가 완료!</h1> <a href='/baroeat_interface.html'>지도로 돌아가기</a>";

        } catch (Exception e) {
            return "<h1>오류 발생: " + e.getMessage() + "</h1>";
        }
    }
}

