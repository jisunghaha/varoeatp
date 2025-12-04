package com.example.demo.controller;

import com.example.demo.domain.Product;
import com.example.demo.domain.Store;
import com.example.demo.domain.StoreTable;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.StoreTableRepository;
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

    @Autowired
    private StoreTableRepository storeTableRepository;

    @PostConstruct
    public void initTestStores() {
        System.out.println("====== [1/3] StoreController 초기화 시작 ======");

        // 1. 매장 카테고리 데이터 보정 (NULL -> 실데이터)
        List<Store> allStores = storeRepository.findAll();
        for (Store store : allStores) {
            if (store.getCategory() == null || store.getCategory().isEmpty()) {
                String name = store.getStoreName();
                if (name.contains("수영국밥") || name.contains("세연정")) {
                    store.setCategory("한식");
                } else if (name.contains("타키온")) {
                    store.setCategory("주점");
                } else {
                    store.setCategory("기타");
                }
                storeRepository.save(store);
                System.out.println("====== [Update] 매장 '" + name + "' 카테고리 업데이트: " + store.getCategory());
            }
        }

        // 테이블 변경으로 인해 메뉴와 테이블 데이터가 없을 수 있으므로 초기화 로직을 실행합니다.
        try {
            initTestProducts();
            initTestTables();
        } catch (Exception e) {
            System.err.println("====== [ERROR] 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("====== [3/3] StoreController 초기화 완료 ======");
    }

    // 👇 [수정됨] 우리가 정한 최신 메뉴와 가격으로 업데이트된 메서드
    public void initTestProducts() {
        System.out.println("====== [2/3-A] 상품 데이터 초기화 확인 중... ======");
        if (productRepository.count() > 0) {
            System.out.println("====== [Skip] 상품 데이터가 이미 존재하여 초기화를 건너뜁니다. ======");
            return;
        }

        List<Store> allStores = storeRepository.findAll();
        if (allStores.isEmpty()) {
            System.out.println("====== [Skip] 매장 정보가 하나도 없어 상품을 추가할 수 없습니다. ======");
            return;
        }

        // DB에 있는 매장 ID를 가져와서 사용 (하드코딩 제거)
        Long storeId1 = allStores.size() > 0 ? allStores.get(0).getId() : null;
        Long storeId2 = allStores.size() > 1 ? allStores.get(1).getId() : null;
        Long storeId3 = allStores.size() > 2 ? allStores.get(2).getId() : null;

        List<Product> products = new ArrayList<>();

        // ==========================================
        // 1. 첫 번째 매장 (예: 수영국밥)
        // ==========================================
        if (storeId1 != null) {
            System.out.println("====== 매장 ID " + storeId1 + "에 상품 추가 중... ======");
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
        }

        // ==========================================
        // 2. 두 번째 매장 (예: 세연정)
        // ==========================================
        if (storeId2 != null) {
            System.out.println("====== 매장 ID " + storeId2 + "에 상품 추가 중... ======");
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
        }

        // ==========================================
        // 3. 세 번째 매장 (예: 타키온)
        // ==========================================
        if (storeId3 != null) {
            System.out.println("====== 매장 ID " + storeId3 + "에 상품 추가 중... ======");
            // [대표메뉴]
            products.add(
                    new Product("닭갈비철판볶음밥 + 계란탕미니", 9900, "든든한 볶음밥과 따뜻한 계란탕 세트", storeId3, "대표메뉴",
                            "dakgalbi_rice.jpg"));
            products.add(
                    new Product("매콤 오돌 무뼈 닭발", 13900, "오독오독 식감이 살아있는 매운 안주", storeId3, "대표메뉴", "chicken_feet.jpg"));
            products.add(new Product("골뱅이소면", 13900, "새콤달콤한 골뱅이 무침과 쫄깃한 소면", storeId3, "대표메뉴", "whelk_noodle.jpg"));

            // [국물요리]
            products.add(new Product("계란탕", 9900, "부드럽고 따뜻한 국물", storeId3, "국물요리", "egg_soup.jpg"));
            products.add(new Product("돼지김치찌개", 11900, "돼지고기가 듬뿍 들어간 얼큰한 찌개", storeId3, "국물요리", "kimchi_stew.jpg"));
            products.add(new Product("오뎅탕", 10900, "시원한 국물의 부산 오뎅탕", storeId3, "국물요리", "odeng_soup.jpg"));

            // [주류]
            products.add(new Product("소주", 4000, "참이슬/진로/대선", storeId3, "주류", "soju.jpg"));
            products.add(new Product("맥주", 4000, "카스/테라 병맥주", storeId3, "주류", "beer.jpg"));
        }

        productRepository.saveAll(products);
        System.out.println("====== 상품 " + products.size() + "개 DB 저장 완료 ======");
    }

    // 3. 테이블(좌석) 초기화
    public void initTestTables() {
        System.out.println("====== [2/3-B] 테이블 데이터 초기화 확인 중... ======");
        if (storeTableRepository.count() > 0) {
            System.out.println("====== [Skip] 테이블 데이터가 이미 존재합니다. ======");
            return;
        }

        List<Store> allStores = storeRepository.findAll();
        if (allStores.isEmpty()) {
            System.out.println("====== [Skip] 매장 정보가 없어 테이블을 추가할 수 없습니다. ======");
            return;
        }

        System.out.println("====== 테이블 데이터 초기화 시작... ======");
        List<StoreTable> allTables = new ArrayList<>();

        // 각 매장마다 테이블 4개씩 추가 (1인석, 2인석, 4인석, 단체석)
        for (Store store : allStores) {
            // 1) 1인석 (혼밥석)
            StoreTable t0 = new StoreTable();
            t0.setName("혼밥석 (바 테이블)");
            t0.setDescription("혼자서도 편안한 바 테이블 좌석");
            t0.setCapacityMin(1);
            t0.setCapacityMax(1);
            t0.setCapacity(1); // Added to satisfy DB schema
            t0.setAdditionalPrice(0);
            t0.setTotalCount(5);
            t0.setStore(store);

            // 2) 2인석
            StoreTable t1 = new StoreTable();
            t1.setName("오붓한 2인석");
            t1.setDescription("데이트하기 좋은 2인 테이블");
            t1.setCapacityMin(2);
            t1.setCapacityMax(2);
            t1.setCapacity(2); // Added to satisfy DB schema
            t1.setAdditionalPrice(0);
            t1.setTotalCount(5);
            t1.setStore(store);

            // 3) 4인석
            StoreTable t2 = new StoreTable();
            t2.setName("편안한 4인석");
            t2.setDescription("가족, 친구와 함께하는 4인 테이블");
            t2.setCapacityMin(3);
            t2.setCapacityMax(4);
            t2.setCapacity(4); // Added to satisfy DB schema
            t2.setAdditionalPrice(0);
            t2.setTotalCount(10);
            t2.setStore(store);

            // 4) 단체석 (8인석)
            StoreTable t3 = new StoreTable();
            t3.setName("단체 회식석");
            t3.setDescription("넓고 쾌적한 8인 단체석");
            t3.setCapacityMin(5);
            t3.setCapacityMax(8);
            t3.setCapacity(8); // Added to satisfy DB schema
            t3.setAdditionalPrice(5000);
            t3.setTotalCount(2);
            t3.setStore(store);

            allTables.add(t0);
            allTables.add(t1);
            allTables.add(t2);
            allTables.add(t3);
        }

        storeTableRepository.saveAll(allTables);
        System.out.println("====== 모든 매장에 테이블 데이터 저장 완료 ======");
    }

    // --- API 엔드포인트 ---

    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return ResponseEntity.ok(stores);
    }

    // 매장 1개 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
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
        if (productId == null)
            return ResponseEntity.badRequest().build();
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