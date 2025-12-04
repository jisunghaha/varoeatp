// --- State Management ---
let currentView = 'home';
let currentUser = null; // Display name (Nickname or Email)
let currentUserEmail = null; // Actual Email
let currentStoreId = null;
let selectedMenus = {};
let menuData = [];
let currentTablePrice = 0;

let notifiedReservations = new Set(); // To prevent duplicate alerts
let currentCategory = 'all'; // Track current menu category
let currentStoreCategory = 'all'; // Track current store category
let favoriteStoreIds = new Set(); // Track favorite stores
let allStores = []; // Cache all stores
const apiUrl = '/api/auth';

// PortOne Init
// PortOne Init
if (window.IMP) {
    IMP.init('imp82508375');
} else {
    console.warn("PortOne IMP not loaded");
}

// Category-based estimated time (in minutes)
const categoryTimeMap = {
    '국밥': 10,
    '찌개': 15,
    '볶음': 15,
    '사이드': 5,
    '음료': 0,
    '주류': 0,
    '기타': 10
};

// --- Navigation Handler ---
function handleNavigate(view, param = null) {
    currentView = view;
    if (view === 'store-detail') currentStoreId = param;
    if (view === 'reservation') {
        selectedMenus = {};
        currentTablePrice = 0;
    }
    render();
}

// --- Render Function ---
function updateHeader() {
    const authButtons = document.getElementById('auth-buttons');
    let navLinks = `
                <button onclick="handleNavigate('home')" class="text-gray-600 hover:text-orange-600 mr-4 font-bold">홈</button>
                <button onclick="handleNavigate('community')" class="text-gray-600 hover:text-orange-600 mr-4 font-bold">커뮤니티</button>
            `;

    if (currentUser) {
        authButtons.innerHTML = navLinks + `
                    <span class="mr-4 text-gray-700">안녕하세요, ${currentUser}님</span>
                    <button onclick="handleNavigate('mypage')" class="text-gray-600 hover:text-orange-600 mr-4">마이페이지</button>
                    <button onclick="handleLogout()" class="bg-gray-200 px-4 py-2 rounded hover:bg-gray-300">로그아웃</button>
                `;
    } else {
        authButtons.innerHTML = navLinks + `
                    <button onclick="handleNavigate('login')" class="text-gray-600 hover:text-orange-600 mr-4">로그인</button>
                    <button onclick="handleNavigate('signup')" class="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600">회원가입</button>
                `;
    }
}

function render() {
    const viewContainer = document.getElementById('view-container');
    const sidebar = document.getElementById('sidebar');
    const map = document.getElementById('map');

    updateHeader();

    // View Routing
    if (currentView === 'home') {
        viewContainer.classList.add('hidden');
        sidebar.classList.remove('hidden');
        map.classList.remove('hidden');
        loadStoresAndMap();
    } else if (currentView === 'community') {
        viewContainer.classList.remove('hidden');
        viewContainer.innerHTML = `
                    <div class="max-w-4xl mx-auto p-4">
                        <div class="flex justify-between items-center mb-6">
                            <h2 class="text-3xl font-bold">커뮤니티</h2>
                            <button onclick="openWriteReviewModal()" class="bg-orange-500 text-white px-4 py-2 rounded font-bold hover:bg-orange-600">글쓰기</button>
                        </div>
                        <div id="review-list" class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <p class="text-gray-500">리뷰를 불러오는 중...</p>
                        </div>
                    
                    <!-- Write Review Modal -->
                    <div id="write-review-modal" class="fixed inset-0 bg-black bg-opacity-50 hidden flex items-center justify-center z-50">
                        <div class="bg-white p-6 rounded-lg w-full max-w-lg">
                            <h3 class="text-xl font-bold mb-4">리뷰 작성</h3>
                            <div class="mb-4">
                                <label class="block font-bold mb-2">매장 선택 (선택사항)</label>
                                <select id="review-store-select" class="w-full p-2 border rounded">
                                    <option value="">매장 선택 안함</option>
                                    <!-- Stores will be loaded here -->
                                </select>
                            </div>
                            <div class="mb-4">
                                <label class="block font-bold mb-2">별점</label>
                                <select id="review-rating" class="w-full p-2 border rounded">
                                    <option value="5">★★★★★</option>
                                    <option value="4">★★★★☆</option>
                                    <option value="3">★★★☆☆</option>
                                    <option value="2">★★☆☆☆</option>
                                    <option value="1">★☆☆☆☆</option>
                                </select>
                            </div>
                            <div class="mb-4">
                                <label class="block font-bold mb-2">내용</label>
                                <textarea id="review-content" class="w-full p-2 border rounded h-32" placeholder="리뷰 내용을 입력하세요"></textarea>
                            </div>
                            <div class="mb-4">
                                <label class="block font-bold mb-2">사진 첨부</label>
                                <input type="file" id="review-image" accept="image/*" class="w-full p-2 border rounded">
                            </div>
                            <div class="flex justify-end space-x-2">
                                <button onclick="closeWriteReviewModal()" class="px-4 py-2 bg-gray-200 rounded">취소</button>
                                <button onclick="submitReview()" class="px-4 py-2 bg-orange-500 text-white rounded font-bold">등록</button>
                            </div>
                        </div>
                    </div>
                </div>`;
        loadCommunity();
    } else if (currentView === 'login') {
        viewContainer.classList.remove('hidden');
        viewContainer.innerHTML = `
                    <div class="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-md">
                        <h2 class="text-2xl font-bold mb-6 text-center">로그인</h2>
                        <input type="email" id="login-email" placeholder="이메일" class="w-full p-2 border rounded mb-4">
                        <input type="password" id="login-password" placeholder="비밀번호" class="w-full p-2 border rounded mb-6">
                        <button onclick="handleBackendLogin()" class="w-full bg-orange-500 text-white p-2 rounded font-bold mb-4">로그인</button>
                        <div class="text-center">
                            <a href="/oauth2/authorization/kakao" class="block w-full bg-yellow-400 text-black p-2 rounded font-bold">카카오 로그인</a>
                        </div>
                        <p class="text-center mt-4 text-sm">계정이 없으신가요? <span onclick="handleNavigate('signup')" class="text-orange-500 cursor-pointer">회원가입</span></p>
                    </div>`;
    } else if (currentView === 'signup') {
        viewContainer.classList.remove('hidden');
        viewContainer.innerHTML = `
                    <div class="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-md">
                        <h2 class="text-2xl font-bold mb-6 text-center">회원가입</h2>
                        <input type="text" id="signup-name" placeholder="이름" class="w-full p-2 border rounded mb-4">
                        <input type="email" id="signup-email" placeholder="이메일" class="w-full p-2 border rounded mb-4">
                        <input type="text" id="signup-phone" placeholder="전화번호" class="w-full p-2 border rounded mb-4">
                        <input type="password" id="signup-password" placeholder="비밀번호" class="w-full p-2 border rounded mb-6">
                        <button onclick="handleBackendSignup()" class="w-full bg-orange-500 text-white p-2 rounded font-bold">가입하기</button>
                    </div>`;
    } else if (currentView === 'mypage') {
        viewContainer.classList.remove('hidden');
        viewContainer.innerHTML = `
                    <div class="max-w-4xl mx-auto p-4">
                        <h2 class="text-3xl font-bold mb-6">마이페이지</h2>
                        <div class="bg-white p-6 rounded-lg shadow mb-6">
                            <h3 class="text-xl font-bold mb-4">내 정보</h3>
                            <p><strong>이름:</strong> <span id="mypage-name"></span></p>
                            <p><strong>이메일:</strong> <span id="mypage-email"></span></p>
                        </div>
                        <div class="bg-white p-6 rounded-lg shadow">
                            <h3 class="text-xl font-bold mb-4">예약 내역</h3>
                            <div id="mypage-reservations" class="space-y-4">
                                <p class="text-gray-500">예약 내역을 불러오는 중...</p>
                            </div>
                        </div>
                    </div>`;
        loadMyPageInfo();
    }
}

function openWriteReviewModal(reservationId = null, storeName = null) {
    if (!currentUser) {
        alert('로그인이 필요합니다.');
        handleNavigate('login');
        return;
    }
    const modal = document.getElementById('write-review-modal');
    const storeSelect = document.getElementById('review-store-select');

    currentReviewReservationId = reservationId;

    if (reservationId) {
        // Writing review for specific reservation
        storeSelect.innerHTML = `<option value="" selected>${storeName}</option>`;
        storeSelect.disabled = true;
    } else {
        // General review (if allowed) or reset
        storeSelect.disabled = false;
        // Reload stores if needed... handled in loadCommunity
    }

    modal.classList.remove('hidden');
}

function closeWriteReviewModal() {
    document.getElementById('write-review-modal').classList.add('hidden');
    document.getElementById('review-content').value = '';
    document.getElementById('review-image').value = '';
    currentReviewReservationId = null;
}

async function submitReview() {
    const content = document.getElementById('review-content').value;
    const rating = parseInt(document.getElementById('review-rating').value);

    if (!content) { alert('내용을 입력하세요'); return; }

    // Image handling (mock for now or implement upload)
    const imageUrl = null;

    const body = {
        content,
        rating,
        imageUrl,
        reservationId: currentReviewReservationId
    };

    try {
        const res = await fetch('/api/reviews', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            alert('리뷰가 등록되었습니다.');
            closeWriteReviewModal();
            loadCommunity();
        } else {
            alert('등록 실패: ' + await res.text());
        }
    } catch (e) {
        alert('오류 발생: ' + e.message);
    }
}

let currentModifyingId = null;
let modifyingMenus = {};
function openModifyModal(id, date, time, partySize, currentMenus) {
    currentModifyingId = id; modifyingMenus = {};
    if (currentMenus) currentMenus.forEach(m => modifyingMenus[m.id] = m.quantity);
    let modal = document.getElementById('modify-modal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'modify-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 hidden';
        modal.innerHTML = `
                <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md max-h-[90vh] overflow-y-auto">
                    <h3 class="text-xl font-bold mb-4">예약 변경</h3>
                    <input type="date" id="mod-date" class="w-full p-2 border rounded mb-2">
                    <input type="time" id="mod-time" class="w-full p-2 border rounded mb-2">
                    <input type="number" id="mod-party-size" class="w-full p-2 border rounded mb-4" min="1">
                    <div id="mod-menu-list" class="space-y-2 max-h-[200px] overflow-y-auto border p-2 rounded mb-4"></div>
                    <div class="flex justify-end space-x-2">
                        <button onclick="document.getElementById('modify-modal').classList.add('hidden')" class="bg-gray-300 px-4 py-2 rounded">취소</button>
                        <button onclick="submitModification()" class="bg-blue-600 text-white px-4 py-2 rounded">변경 확정</button>
                    </div>
                </div>`;
        document.body.appendChild(modal);
    }
    document.getElementById('mod-date').value = date;
    document.getElementById('mod-time').value = time;
    document.getElementById('mod-party-size').value = partySize;
    const listEl = document.getElementById('mod-menu-list');
    if (menuData.length > 0) {
        listEl.innerHTML = '';
        menuData.forEach(p => {
            const qty = modifyingMenus[p.id] || 0;
            listEl.innerHTML += `<div class="flex justify-between items-center text-sm"><span>${p.name}</span><div class="flex items-center space-x-1"><button onclick="updateModMenuQty(${p.id}, -1)" class="px-2 bg-gray-200 rounded">-</button><span id="mod-qty-${p.id}">${qty}</span><button onclick="updateModMenuQty(${p.id}, 1)" class="px-2 bg-orange-200 rounded">+</button></div></div>`;
        });
    } else listEl.innerHTML = '메뉴 정보 없음';
    modal.classList.remove('hidden');
}

function updateModMenuQty(pid, d) { modifyingMenus[pid] = (modifyingMenus[pid] || 0) + d; if (modifyingMenus[pid] < 0) modifyingMenus[pid] = 0; document.getElementById(`mod-qty-${pid}`).textContent = modifyingMenus[pid]; }

async function submitModification() {
    const date = document.getElementById('mod-date').value;
    const time = document.getElementById('mod-time').value;
    const partySize = document.getElementById('mod-party-size').value;
    const menus = [];
    for (const [pid, qty] of Object.entries(modifyingMenus)) if (qty > 0) menus.push({ productId: parseInt(pid), quantity: qty });
    await fetch(`/api/reservations/${currentModifyingId}`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ date, time, partySize: parseInt(partySize), menus })
    });
    document.getElementById('modify-modal').classList.add('hidden');
    loadMyPageInfo();
}

async function loadAvailableTimes() {
    const date = document.getElementById('res-date').value;
    const timeSelect = document.getElementById('res-time');
    try {
        const res = await fetch(`/api/reservations/times?date=${date}`);
        const times = await res.json();
        timeSelect.innerHTML = '<option value="">시간 선택</option>';
        times.forEach(t => {
            const opt = document.createElement('option'); opt.value = t.time; opt.textContent = `${t.time} (${t.status})`; if (t.status === 'full') opt.disabled = true;
            timeSelect.appendChild(opt);
        });
        timeSelect.disabled = false;
    } catch (e) { }
}

async function loadAvailableTables() {
    const date = document.getElementById('res-date').value;
    const time = document.getElementById('res-time').value;
    const partySize = document.getElementById('res-party-size').value;
    const listEl = document.getElementById('res-table-list');
    try {
        const res = await fetch(`/api/reservations/tables?storeId=${currentStoreId}&date=${date}&time=${time}&partySize=${partySize}`);
        const tables = await res.json();
        listEl.innerHTML = '';
        tables.forEach(t => {
            listEl.innerHTML += `<div class="p-3 border rounded hover:bg-orange-50 cursor-pointer flex justify-between items-center"><div><span class="font-bold">${t.name}</span> <span class="text-sm">(${t.capacity})</span></div><button onclick="selectTable(this.closest('div'), ${t.id}, ${t.price})" class="bg-orange-500 text-white text-xs px-3 py-1 rounded">선택</button></div>`;
        });
        // Refresh menu to apply time restrictions
        filterMenu(currentCategory);
    } catch (e) { }
}

function selectTable(el, id, price) {
    const siblings = el.parentElement.children;
    for (let s of siblings) { s.classList.remove('bg-orange-100', 'border-orange-500'); s.querySelector('button').textContent = '선택'; s.querySelector('button').classList.replace('bg-green-600', 'bg-orange-500'); }
    el.classList.add('bg-orange-100', 'border-orange-500');
    el.querySelector('button').textContent = '선택됨';
    el.querySelector('button').classList.replace('bg-orange-500', 'bg-green-600');
    modal = document.createElement('div');
    modal.id = 'receipt-modal';
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    document.body.appendChild(modal);
}
modal.innerHTML = `
                <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                    <div class="flex justify-between items-center mb-6 border-b pb-4">
                        <h3 class="text-2xl font-bold text-orange-600">영수증</h3>
                        <button onclick="document.getElementById('receipt-modal').remove()" class="text-gray-500 hover:text-black text-xl">&times;</button>
                    </div>
                    <div class="space-y-4">
                        <div class="flex justify-between">
                            <span class="text-gray-600">매장명</span>
                            <span class="font-bold">${res.storeName}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-600">일시</span>
                            <span>${res.date} ${res.time}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-600">인원</span>
                            <span>${res.partySize}명</span>
                        </div>
                        <div class="border-t pt-4 mt-4">
                            <h4 class="font-bold mb-2">주문 내역</h4>
                            ${res.menus && res.menus.length > 0 ? res.menus.map(m => `<div class="flex justify-between text-sm"><span class="text-gray-600">${m.name} x ${m.quantity}</span><span>${(m.price * m.quantity).toLocaleString()}원</span></div>`).join('') : '<p class="text-sm text-gray-400">주문 메뉴 없음</p>'}
                        </div>
                        <div class="flex justify-between border-t pt-4 mt-4 font-bold text-lg">
                            <span>총 결제금액</span>
                            <span class="text-orange-600">${res.totalPrice.toLocaleString()}원</span>
                        </div>
                        <div class="mt-4 p-3 bg-gray-100 rounded text-center">
                            <p class="font-bold ${res.paymentMethod === 'ON_SITE' ? 'text-orange-600' : 'text-blue-600'}">
                                ${res.paymentMethod === 'ON_SITE' ? '결제 예정 (현장 결제)' : '결제 완료 (카드)'}
                            </p>
                            <p class="text-xs text-gray-500 mt-1">${res.paymentTime ? res.paymentTime.replace('T', ' ').substring(0, 16) : ''}</p>
                        </div>
                    </div>
                    <div class="mt-6 text-center text-xs text-gray-400">
                        본 영수증은 바로잇 결제 증빙용입니다.
                    </div>
                </div>
            `;
        }

async function loadStoresAndMap() {
    const listEl = document.getElementById('store-list-sidebar');
    try {
        const response = await fetch('/api/stores');
        const stores = await response.json();
        allStores = stores; // Cache
        listEl.innerHTML = '';

        // Map Init
        let map = null;
        if (window.kakao && window.kakao.maps) {
            const container = document.getElementById('map');
            const options = { center: new kakao.maps.LatLng(37.5665, 126.9780), level: 3 };
            map = new kakao.maps.Map(container, options);
        } else {
            document.getElementById('map').innerHTML = '<div class="flex items-center justify-center h-full text-gray-500">지도를 불러올 수 없습니다 (API Key 확인 필요)</div>';
        }

        stores.forEach(store => {
            // Sidebar Item
            const item = document.createElement('div');
            item.className = 'p-4 border-b hover:bg-orange-50 cursor-pointer store-item';
            item.dataset.cat = store.category;
            item.onclick = () => handleNavigate('store-detail', store.id);
            item.innerHTML = `<h3 class="font-bold text-lg">${store.storeName}</h3><p class="text-sm text-gray-600">${store.category} | ${store.address}</p><p class="text-xs text-orange-500">${store.isOpen ? '영업중' : '준비중'}</p>`;
            listEl.appendChild(item);

            // Map Marker
            if (map && window.kakao && window.kakao.maps) {
                const markerPosition = new kakao.maps.LatLng(store.lat, store.lng);
                const marker = new kakao.maps.Marker({ position: markerPosition });
                marker.setMap(map);

                // InfoWindow
                const iwContent = `<div style="padding:5px;">${store.storeName} <br><a href="#" onclick="handleNavigate('store-detail', ${store.id}); return false;" style="color:blue">상세보기</a></div>`;
                const infowindow = new kakao.maps.InfoWindow({ content: iwContent });

                kakao.maps.event.addListener(marker, 'click', function () {
                    infowindow.open(map, marker);
                });
            }
        });
    } catch (e) {
        console.error("Store load failed", e);
        listEl.innerHTML = '<p class="text-center text-red-500 p-4">매장 정보를 불러오지 못했습니다.</p>';
    }
}

// --- Notification Logic ---
async function checkUpcomingReservations() {
    if (!currentUser) return;
    try {
        const response = await fetch('/api/reservations/my');
        if (!response.ok) return;
        const reservations = await response.json();

        const now = new Date();
        reservations.forEach(res => {
            const resTime = new Date(`${res.date}T${res.time}`);
            const diffMs = resTime - now;
            const diffMins = Math.floor(diffMs / 60000);

            // Notify if within 30 mins (0 to 30) and not notified yet
            if (diffMins > 0 && diffMins <= 30 && !notifiedReservations.has(res.id)) {
                alert(`[알림] 예약 시간이 ${diffMins}분 남았습니다!\n매장: ${res.storeName} \n시간: ${res.time}`);
                notifiedReservations.add(res.id);
            }
        });
    } catch (e) { console.error("Notification check failed", e); }
}

// --- Initialization ---
// Check reservations every minute
setInterval(checkUpcomingReservations, 60000);

// Initial Load
loadStoresAndMap();
checkUpcomingReservations();
