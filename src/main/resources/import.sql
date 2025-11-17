/* 서버가 켜질 때마다 3개의 매장을 자동으로 추가합니다. (전화번호, 영업여부 포함) */
INSERT INTO store (store_name, address, lat, lng, phone_number, is_open) VALUES ('수영국밥', '부산 부산진구 가야공원로 59 1,2층', 35.148018045351755, 129.03018854366422, '0507-1352-8297', true);
INSERT INTO store (store_name, address, lat, lng, phone_number, is_open) VALUES ('세연정 가야점', '부산 부산진구 가야대로 554', 35.15361525239585, 129.03268398179472, '051-867-2000', true);
INSERT INTO store (store_name, address, lat, lng, phone_number, is_open) VALUES ('타키온', '부산 부산진구 대학로 76 1층', 35.14905930709196, 129.03446054618996, '051-891-1009', true);