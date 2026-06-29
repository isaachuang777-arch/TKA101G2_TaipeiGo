const { createApp, ref, computed, onMounted } = Vue;

createApp({
    setup() {
        const ticket = ref({});
        const isLoaded = ref(false);

        // 我的最愛
        const isFavorite = ref(false);

        // 圖片預覽
        const showLightbox = ref(false);
        const slideIndex = ref(0);

        // 日期選擇
        const selectedDateType = ref('tomorrow'); // 'tomorrow' | 'dayAfterTomorrow' | 'threeDaysLater' | 'calendar'
        const selectedDateParam = ref('');
        let fpInstance = null;

        // 門票購買數量
        const quantities = ref({
            adult: 0,
            child: 0,
            concession: 0
        });

        // 格式化日期為 YYYY-MM-DD
        const formatDateStr = (date) => {
            const yyyy = date.getFullYear();
            const mm = String(date.getMonth() + 1).padStart(2, '0');
            const dd = String(date.getDate()).padStart(2, '0');
            return `${yyyy}-${mm}-${dd}`;
        };

        // 取得日期的字串
        const getQuickDateLabel = (daysOffset) => {
            const target = new Date();
            target.setDate(target.getDate() + daysOffset);
            return `${target.getMonth() + 1}月${target.getDate()}日`;
        };

        const dayAfterTomorrowLabel = ref(getQuickDateLabel(2));
        const threeDaysLaterLabel = ref(getQuickDateLabel(3));

        // 總張數
        const totalTicketsCount = computed(() => {
            return quantities.value.adult + quantities.value.child + quantities.value.concession;
        });

        // 總金額
        const totalPriceAmount = computed(() => {
            const adultTotal = quantities.value.adult * (ticket.value.adultPrice || 0);
            const childTotal = quantities.value.child * (ticket.value.childPrice || 0);
            const concessionTotal = quantities.value.concession * (ticket.value.concessionPrice || 0);
            return adultTotal + childTotal + concessionTotal;
        });

        // 檢查是否已登入，若未登入則導向登入頁
        const checkLogin = async () => {
            try {
                const res = await fetch('../api/auth/me');
                if (res.ok) {
                    const data = await res.json();
                    if (data.login) {
                        return true;
                    }
                }
            } catch (err) {
                // console.error('檢查登入狀態失敗:', err);
            }
            // 未登入，提示並導向登入頁面
            alert('請先登入會員！');
            const currentUrl = window.location.href;
            window.location.href = `../auth/login?redirect=${encodeURIComponent(currentUrl)}`;
        };

        // 檢查庫存是否足夠
        const checkStock = async (ticketId, quantity) => {
            try {
                const stockRes = await fetch(`/api/tickets/checkStock?ticketId=${ticketId}&quantity=${quantity}`);
                if (stockRes.ok) {
                    const stockData = await stockRes.json();
                    if (stockData.status === 'success') {
                        if (stockData.data) {
                            return true;
                        } else {
                            alert('庫存不足，無法加入購物車');
                            return false;
                        }
                    }
                }
                alert('檢查庫存失敗，請稍後再試');
                return false;
            } catch (err) {
                alert('檢查庫存發生錯誤，請稍後再試');
                return false;
            }
        };

        // 呼叫 API 將商品加入購物車
        const addToCart = async (items) => {
            try {
                let allSuccess = true;
                for (const item of items) {
                    const res = await fetch('/frontend/cart/insertCart', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(item)
                    });
                    if (res.ok) {
                        const resultText = await res.text();
                        if (resultText !== 'success') {
                            allSuccess = false;
                        }
                    } else {
                        allSuccess = false;
                    }
                }
                return allSuccess;
            } catch (err) {
                // console.error('加入購物車發生錯誤:', err);
                return false;
            }
        };

        // 整理已選擇的門票資料
        const prepareBookingItems = () => {
            const items = [];
            if (quantities.value.adult > 0) {
                items.push({
                    productId: ticket.value.ticketId,
                    productQuantity: quantities.value.adult,
                    expiryDate: `${selectedDateParam.value}T23:59:59`,
                    productType: 'TICKET',
                    spec: 'ADULT'
                });
            }
            if (quantities.value.child > 0) {
                items.push({
                    productId: ticket.value.ticketId,
                    productQuantity: quantities.value.child,
                    expiryDate: `${selectedDateParam.value}T23:59:59`,
                    productType: 'TICKET',
                    spec: 'CHILD'
                });
            }
            if (quantities.value.concession > 0) {
                items.push({
                    productId: ticket.value.ticketId,
                    productQuantity: quantities.value.concession,
                    expiryDate: `${selectedDateParam.value}T23:59:59`,
                    productType: 'TICKET',
                    spec: 'CONCESSION'
                });
            }
            return items;
        };

        // 處理加入購物車
        const handleAddToCart = async () => {
            const loggedIn = await checkLogin();
            if (!loggedIn) return;

            if (!validateBooking()) return;

            // 檢查庫存是否足夠
            const hasStock = await checkStock(ticket.value.ticketId, totalTicketsCount.value);
            if (!hasStock) return;

            // 整理已選擇的門票資料
            const items = prepareBookingItems();

            // 呼叫 API 將商品加入購物車
            const success = await addToCart(items);
            if (success) {
                alert('成功加入購物車');
                // 重置畫面上數量
                quantities.value.adult = 0;
                quantities.value.child = 0;
                quantities.value.concession = 0;

                // 更新 header 購物車數量
                if (typeof loadCartCount === "function") {
                    loadCartCount();
                }
            } else {
                alert('加入購物車失敗，請稍後再試');
            }
        };

        // 處理立即購買
        const handleBuyNow = async () => {
            const loggedIn = await checkLogin();
            if (!loggedIn) return;

            if (!validateBooking()) return;
            // TODO: 串接 API
        };

        // 處理加入我的最愛 (切換我的最愛狀態)
        const handleAddToFavorite = async () => {
            const loggedIn = await checkLogin();
            if (!loggedIn) return;

            try {
                const res = await fetch(`../favorite/toggle/ajax?type=TICKET&id=${ticket.value.ticketId}`, {
                    method: 'POST'
                });
                if (res.ok) {
                    isFavorite.value = await res.json();
                } else {
                    alert('操作失敗，請稍後再試');
                }
            } catch (err) {
                alert('操作失敗，請稍後再試');
            }
        };

        // 判斷此門票是否在該會員的收藏清單中
        const checkFavorite = async (ticketId) => {
            // 此 api 裡就有方法判斷是否有登入，不用額外處理判斷 
            try {
                const res = await fetch(`../favorite/check?type=TICKET&id=${ticketId}`);
                if (res.ok) {
                    isFavorite.value = await res.json();
                }
            } catch (err) {
                // console.error('檢查收藏失敗:', err);
            }
        };

        // 載入單筆門票詳細資料
        const loadTicketDetails = async (ticketId) => {
            try {
                const res = await fetch(`../api/tickets/info?ticketId=${ticketId}`);
                if (!res.ok) {
                    const errorData = await res.json().catch(() => ({}));
                    throw new Error(errorData.message || '找不到此門票商品');
                }
                const result = await res.json();
                if (result.status === 'success') {
                    ticket.value = result.data;
                    document.title = `${ticket.value.ticketName} - 台北GO了沒`;
                    isLoaded.value = true;

                    // 檢查使用者是否已登入，若已登入則查詢其是否收藏了此商品
                    await checkFavorite(ticketId);


                } else {
                    throw new Error(result.message || '找不到此門票商品');
                }
            } catch (err) {
                //console.error('載入門票詳情錯誤:', err);
                alert(err.message || '載入門票詳情錯誤');
                window.location.href = '../ticket';
            }
        };

        // 圖片預覽
        const openLightbox = (index) => {
            showLightbox.value = true;
            slideIndex.value = index;
        };

        const closeLightbox = () => {
            showLightbox.value = false;
        };

        const changeSlide = (n) => {
            let newIdx = slideIndex.value + n;
            const length = ticket.value.imageUrls ? ticket.value.imageUrls.length : 0;
            if (newIdx >= length) newIdx = 0;
            if (newIdx < 0) newIdx = length - 1;
            slideIndex.value = newIdx;
        };

        const currentSlide = (idx) => {
            slideIndex.value = idx;
        };



        // 日期切換
        const selectQuickDate = (type) => {
            selectedDateType.value = type;
            if (fpInstance) {
                fpInstance.clear();
                const openCalendarBtn = document.getElementById("openCalendarBtn");
                if (openCalendarBtn) openCalendarBtn.innerText = "所有日期";
            }

            const today = new Date();
            let targetDate = new Date();

            if (type === 'tomorrow') {
                targetDate.setDate(today.getDate() + 1);
            } else if (type === 'dayAfterTomorrow') {
                targetDate.setDate(today.getDate() + 2);
            } else if (type === 'threeDaysLater') {
                targetDate.setDate(today.getDate() + 3);
            }

            selectedDateParam.value = formatDateStr(targetDate);
        };

        // 門票數量加減
        const changeQty = (type, amount) => {
            let currentVal = quantities.value[type] || 0;
            currentVal += amount;
            if (currentVal < 0) currentVal = 0;
            if (currentVal > 99) currentVal = 99;
            quantities.value[type] = currentVal;
        };

        // 購買數量驗證
        const validateBooking = () => {
            if (totalTicketsCount.value === 0) {
                alert("請至少選擇一個方案並增加數量！");
                return false;
            }
            return true;
        };

        onMounted(() => {
            // 從 URL 解析 ticketId
            const urlParams = new URLSearchParams(window.location.search);
            let ticketId = urlParams.get('ticketId');
            if (ticketId) {
                // 去除可能不小心多打的等號或空白
                ticketId = ticketId.replace('=', '').trim();
                loadTicketDetails(ticketId);
            }

            // 設定「明日」日期
            const tomorrowDate = new Date();
            tomorrowDate.setDate(tomorrowDate.getDate() + 1);
            selectedDateParam.value = formatDateStr(tomorrowDate);

            // 初始化 Flatpickr 日曆
            fpInstance = flatpickr("#inlineDatePicker", {
                locale: "zh_tw",
                minDate: "today",
                dateFormat: "Y-m-d",
                disableMobile: "true",
                static: true,
                appendTo: document.querySelector(".calendar-wrapper"),
                onChange: function (selectedDates, dateStr, instance) {
                    if (selectedDates.length > 0) {
                        const openCalendarBtn = document.getElementById("openCalendarBtn");
                        if (openCalendarBtn) openCalendarBtn.innerText = dateStr;
                        selectedDateParam.value = dateStr;
                        selectedDateType.value = 'calendar';
                    }
                }
            });

            // 點擊日期按鈕開啟日曆
            const openCalBtn = document.getElementById("openCalendarBtn");
            if (openCalBtn) {
                openCalBtn.addEventListener("click", function (e) {
                    e.stopPropagation();
                    if (fpInstance) fpInstance.open();
                });
            }
        });

        // 提供給html
        return {
            ticket,
            isLoaded,
            isFavorite,
            showLightbox,
            slideIndex,
            selectedDateType,
            selectedDateParam,
            quantities,
            dayAfterTomorrowLabel,
            threeDaysLaterLabel,
            totalTicketsCount,
            totalPriceAmount,
            checkLogin,
            openLightbox,
            closeLightbox,
            changeSlide,
            currentSlide,
            selectQuickDate,
            changeQty,
            handleAddToCart,
            handleBuyNow,
            handleAddToFavorite
        };
    }
}).mount('#app');