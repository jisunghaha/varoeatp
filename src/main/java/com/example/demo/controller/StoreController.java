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

    // 서버 시작 시 데이터 초기화 자동 실행
    @PostConstruct
    public void initData() {
        initTestStores();    // 1. 매장 생성
        initTestProducts();  // 2. 메뉴 생성
        initTestTables();    // 3. 테이블 생성 (수정됨)
    }

    // 1. 매장 초기화
    public void initTestStores() {
        if (storeRepository.count() > 0) {
            System.out.println("====== [Skip] 매장 데이터가 이미 존재합니다. ======");
            return;
        }

        System.out.println("====== 매장 데이터 초기화 시작... ======");
        try {
            Store s1 = new Store("수영국밥", "부산 부산진구 가야공원로 59 1,2층", 35.147957, 129.030187, "0507-1352-8297", true);
            Store s2 = new Store("세연정 가야점", "부산 부산진구 가야대로 554", 35.153613, 129.032678, "051-867-2000", true);
            Store s3 = new Store("타키온", "부산 부산진구 대학로 76 1층", 35.149059, 129.034466, "051-891-1009", true);

            storeRepository.saveAll(List.of(s1, s2, s3));
            System.out.println("====== 매장 3개 DB 저장 완료 ======");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. 상품(메뉴) 초기화
    public void initTestProducts() {
        if (productRepository.count() > 0) {
            System.out.println("====== [Skip] 상품 데이터가 이미 존재합니다. ======");
            return;
        }
        if (storeRepository.count() == 0) return;

        System.out.println("====== 상품 데이터 초기화 시작... ======");

        List<Store> stores = storeRepository.findAll();
        Long id1 = stores.get(0).getId(); // 수영국밥
        Long id2 = stores.get(1).getId(); // 세연정
        Long id3 = stores.get(2).getId(); // 타키온

        List<Product> products = new ArrayList<>();

        // [수영국밥 메뉴]
        products.add(new Product("돼지 국밥", 11000, "진한 사골 육수의 깊은 맛", id1, "국밥", "pork_soup.jpg"));
        products.add(new Product("순대 국밥", 11000, "통통한 전통 순대가 가득", id1, "국밥", "sundae_soup.jpg"));
        products.add(new Product("내장 국밥", 11000, "쫄깃하고 고소한 내장", id1, "국밥", "offal_soup.jpg"));
        products.add(new Product("섞어 국밥", 11000, "고기, 순대, 내장을 한 번에", id1, "국밥", "mix_soup.jpg"));
        products.add(new Product("맛보기 수육", 13000, "야들야들 부드러운 수육", id1, "사이드 메뉴", "suyuk.jpg"));
        products.add(new Product("맛보기 순대", 13000, "속이 꽉 찬 맛보기 순대", id1, "사이드 메뉴", "sundae_plate.jpg"));
        products.add(new Product("식혜", 2500, "살얼음 동동 수제 식혜", id1, "음료", "sikhye.jpg"));

        // [세연정 메뉴]
        products.add(new Product("토마호그세트", 142000, "프리미엄 스테이크", id2, "대표메뉴", "tomahawk.jpg"));
        products.add(new Product("갈비살 소금구이", 35000, "고소한 육즙", id2, "대표메뉴", "special_ribs.jpg"));
        products.add(new Product("양념갈비3대", 35000, "특제 양념 숙성 갈비", id2, "대표메뉴", "seasoned_ribs.jpg"));
        products.add(new Product("화로 소불고기", 22000, "화로 향 가득한 불고기", id2, "대표메뉴", "bulgogi.jpg"));
        products.add(new Product("콜라", 2000, "시원한 콜라", id2, "음료", "coke.jpg"));

        // [타키온 메뉴]
        products.add(new Product("닭갈비철판볶음밥", 9900, "든든한 볶음밥 세트", id3, "대표메뉴", "dakgalbi_rice.jpg"));
        products.add(new Product("무뼈 닭발", 13900, "매콤한 무뼈 닭발", id3, "대표메뉴", "chicken_feet.jpg"));
        products.add(new Product("골뱅이소면", 13900, "새콤달콤 골뱅이 무침", id3, "대표메뉴", "whelk_noodle.jpg"));
        products.add(new Product("계란탕", 9900, "부드러운 계란탕", id3, "국물요리", "egg_soup.jpg"));
        products.add(new Product("오뎅탕", 10900, "시원한 부산 오뎅탕", id3, "국물요리", "odeng_soup.jpg"));
        products.add(new Product("맥주", 4000, "시원한 병맥주", id3, "주류", "beer.jpg"));
        products.add(new Product("소주", 4000, "참이슬/진로", id3, "주류", "soju.jpg"));

        productRepository.saveAll(products);
        System.out.println("====== 상품 " + products.size() + "개 DB 저장 완료 ======");
    }

    // 3. 테이블(좌석) 초기화 [수정됨]
    public void initTestTables() {
        if (storeTableRepository.count() > 0) {
            System.out.println("====== [Skip] 테이블 데이터가 이미 존재합니다. ======");
            return;
        }
        // 매장 정보가 없으면 테이블을 연결할 수 없으므로 종료
        if (storeRepository.count() == 0) return;

        System.out.println("====== 테이블 데이터 초기화 시작... ======");

        // 모든 매장을 가져옵니다. (테이블을 모든 매장에 똑같이 넣어주기 위함)
        List<Store> allStores = storeRepository.findAll();
        List<StoreTable> allTables = new ArrayList<>();

        // 각 매장마다 테이블 3개씩 추가 (2인석, 4인석, 단체석)
        for (Store store : allStores) {
            // 1) 2인석
            StoreTable t1 = new StoreTable();
            t1.setName("연인석 (창가)");
            t1.setDescription("뷰가 좋은 창가 2인석");
            t1.setCapacityMin(1);
            t1.setCapacityMax(2);
            t1.setAdditionalPrice(0);
            t1.setTotalCount(5);
            t1.setStore(store); // 👈 중요: 매장 연결!

            // 2) 4인석
            StoreTable t2 = new StoreTable();
            t2.setName("일반 4인석");
            t2.setDescription("편안한 소파 좌석");
            t2.setCapacityMin(2);
            t2.setCapacityMax(4);
            t2.setAdditionalPrice(0);
            t2.setTotalCount(10);
            t2.setStore(store); // 👈 중요: 매장 연결!

            // 3) 단체석
            StoreTable t3 = new StoreTable();
            t3.setName("단체 룸");
            t3.setDescription("프라이빗한 단체 룸");
            t3.setCapacityMin(5);
            t3.setCapacityMax(8);
            t3.setAdditionalPrice(5000);
            t3.setTotalCount(2);
            t3.setStore(store); // 👈 중요: 매장 연결!

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
        return ResponseEntity.ok(storeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID " + id + "에 해당하는 매장을 찾을 수 없습니다."));
        return ResponseEntity.ok(store);
    }

    @GetMapping("/{storeId}/products")
    public ResponseEntity<List<Product>> getProductsByStoreId(@PathVariable Long storeId) {
        List<Product> products = productRepository.findByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductDetail(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));
        return ResponseEntity.ok(product);
    }

    // 수동 초기화 트리거
    @GetMapping("/init-products")
    public ResponseEntity<String> manualInit() {
        try {
            initData();
            return ResponseEntity.ok("초기화 작업 시도 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("초기화 실패: " + e.getMessage());
        }
    }
}