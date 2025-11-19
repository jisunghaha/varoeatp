package com.example.demo.controller;

import com.example.demo.domain.Product;
import com.example.demo.domain.Store;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostConstruct // 서버 시작 시 1회 자동 실행
    public void initTestStores() {
        if (storeRepository.count() == 0) {
            System.out.println("====== DB가 비어있어, 매장 3개 추가를 시작합니다... ======");
            try {
                // 1. 수영국밥 (최신 주소 및 좌표 반영)
                Store testStore1 = new Store(
                        "수영국밥",
                        "부산 부산진구 가야공원로 59 1,2층",
                        35.14795724176053, 129.03018703293802,
                        "0507-1352-8297", true
                );
                // 2. 세연정 (가야점)
                Store testStore2 = new Store(
                        "세연정 가야점",
                        "부산 부산진구 가야대로 554",
                        35.15361309245611, 129.0326784417889,
                        "051-867-2000", true
                );
                // 3. 타키온
                Store testStore3 = new Store(
                        "타키온",
                        "부산 부산진구 대학로 76 1층",
                        35.149059214982096, 129.0344660298509,
                        "051-891-1009", true
                );

                storeRepository.save(testStore1);
                storeRepository.save(testStore2);
                storeRepository.save(testStore3);

                System.out.println("====== 매장 3개 DB에 자동 추가 완료 ======");

                // 매장 추가 후 상품 초기화 시도
                initTestProducts();

            } catch (Exception e) {
                System.out.println("====== [오류] 매장 추가 중 실패: " + e.getMessage() + " ======");
            }
        } else {
            System.out.println("====== DB에 이미 데이터가 있으므로, 매장 추가를 건너뜁니다. ======");
        }
    }

    // 👇 [수정됨] 우리가 정한 최신 메뉴와 가격으로 업데이트된 메서드
    public void initTestProducts() {
        if (productRepository.count() > 0) {
            System.out.println("====== [경고] 상품 데이터가 이미 존재하여 초기화를 건너뜁니다. ======");
            return;
        }

        Long storeId1 = 1L; // 수영국밥
        Long storeId2 = 2L; // 세연정
        Long storeId3 = 3L; // 타키온

        List<Product> products = new ArrayList<>();

        // ==========================================
        // 1. 수영국밥 메뉴 (Store ID: 1)
        // ==========================================

        // [국밥]
        products.add(new Product("돼지 국밥", 11000, "진한 사골 육수의 깊은 맛", storeId1, "국밥", "pork_soup.jpg"));
        products.add(new Product("순대 국밥", 11000, "통통한 전통 순대가 가득", storeId1, "국밥", "sundae_soup.jpg"));
        products.add(new Product("내장 국밥", 11000, "쫄깃하고 고소한 내장", storeId1, "국밥", "offal_soup.jpg"));
        products.add(new Product("섞어 국밥", 11000, "고기, 순대, 내장을 한 번에", storeId1, "국밥", "mix_soup.jpg"));

        // [사이드 메뉴]
        products.add(new Product("맛보기 수육", 13000, "야들야들 부드러운 수육 한 접시", storeId1, "사이드 메뉴", "suyuk.jpg"));
        products.add(new Product("맛보기 순대", 13000, "속이 꽉 찬 맛보기 순대", storeId1, "사이드 메뉴", "sundae_plate.jpg"));
        products.add(new Product("보쌈 김치", 3000, "매콤달콤 아삭한 김치 추가", storeId1, "사이드 메뉴", "kimchi.jpg"));

        // [음료]
        products.add(new Product("식혜", 2500, "살얼음 동동 수제 식혜", storeId1, "음료", "sikhye.jpg"));
        products.add(new Product("코카콜라", 2000, "톡 쏘는 시원함", storeId1, "음료", "coke.jpg"));
        products.add(new Product("사이다", 2000, "청량한 사이다", storeId1, "음료", "cider.jpg"));
        products.add(new Product("맥주", 4000, "카스/테라 병맥주", storeId1, "음료", "beer.jpg"));
        products.add(new Product("소주", 4000, "대선/진로/참이슬", storeId1, "음료", "soju.jpg"));


        // ==========================================
        // 2. 세연정 메뉴 (Store ID: 2)
        // ==========================================

        // [대표메뉴]
        products.add(new Product("토마호그세트", 142000, "압도적인 비주얼과 맛의 프리미엄 스테이크", storeId2, "대표메뉴", "tomahawk.jpg"));
        products.add(new Product("화로 소불고기", 22000, "화로 향이 가득한 부드러운 소불고기", storeId2, "대표메뉴", "bulgogi.jpg"));
        products.add(new Product("특갈비살 소금구이", 35000, "고소한 육즙이 터지는 특갈비살", storeId2, "대표메뉴", "special_ribs.jpg"));
        products.add(new Product("양념갈비3대", 35000, "특제 양념으로 숙성시킨 갈비", storeId2, "대표메뉴", "seasoned_ribs.jpg"));
        products.add(new Product("주물럭", 25000, "입맛을 돋우는 매콤달콤 주물럭", storeId2, "대표메뉴", "jumulleok.jpg"));

        // [음료]
        products.add(new Product("콜라", 2000, "톡 쏘는 시원함", storeId2, "음료", "coke.jpg"));
        products.add(new Product("제로 콜라", 2000, "칼로리 걱정 없는 제로 콜라", storeId2, "음료", "zero_coke.jpg"));
        products.add(new Product("사이다", 2000, "청량한 사이다", storeId2, "음료", "cider.jpg"));
        products.add(new Product("제로 사이다", 2000, "깔끔한 제로 사이다", storeId2, "음료", "zero_cider.jpg"));


        // ==========================================
        // 3. 타키온 메뉴 (Store ID: 3)
        // ==========================================

        // [대표메뉴]
        products.add(new Product("닭갈비철판볶음밥 + 계란탕미니", 9900, "든든한 볶음밥과 따뜻한 계란탕 세트", storeId3, "대표메뉴", "dakgalbi_rice.jpg"));
        products.add(new Product("매콤 오돌 무뼈 닭발", 13900, "오독오독 식감이 살아있는 매운 안주", storeId3, "대표메뉴", "chicken_feet.jpg"));
        products.add(new Product("골뱅이소면", 13900, "새콤달콤한 골뱅이 무침과 쫄깃한 소면", storeId3, "대표메뉴", "whelk_noodle.jpg"));

        // [국물요리]
        products.add(new Product("계란탕", 9900, "부드럽고 따뜻한 국물", storeId3, "국물요리", "egg_soup.jpg"));
        products.add(new Product("돼지김치찌개", 11900, "돼지고기가 듬뿍 들어간 얼큰한 찌개", storeId3, "국물요리", "kimchi_stew.jpg"));
        products.add(new Product("오뎅탕", 10900, "시원한 국물의 부산 오뎅탕", storeId3, "국물요리", "odeng_soup.jpg"));

        // [주류]
        products.add(new Product("소주", 4000, "참이슬/진로/대선", storeId3, "주류", "soju.jpg"));
        products.add(new Product("맥주", 4000, "카스/테라 병맥주", storeId3, "주류", "beer.jpg"));


        productRepository.saveAll(products);
        System.out.println("====== 테스트 상품 " + productRepository.count() + "개 DB에 자동 추가 완료 ======");
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

    // 매장 ID별 상품 목록 조회 API
    @GetMapping("/{storeId}/products")
    public ResponseEntity<List<Product>> getProductsByStoreId(@PathVariable Long storeId) {
        List<Product> products = productRepository.findByStoreId(storeId);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    // 상품 상세 조회 API
    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductDetail(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));
        return ResponseEntity.ok(product);
    }

    // 테스트 상품 데이터를 수동으로 삽입하는 API
    @GetMapping("/init-products")
    public ResponseEntity<String> addProductsManually() {
        try {
            initTestProducts();
            return ResponseEntity.ok("상품 테스트 데이터 삽입 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 테스트 데이터 삽입 실패: " + e.getMessage());
        }
    }

    @GetMapping("/add-test-store")
    public String addTestStore() {
        return "<h1>이 기능은 이제 서버 시작 시 @PostConstruct로 자동 실행됩니다.</h1>";
    }
}