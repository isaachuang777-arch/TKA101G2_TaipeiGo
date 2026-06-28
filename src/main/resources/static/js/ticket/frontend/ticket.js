const { createApp, ref, onMounted } = Vue;

createApp({
    setup() {
        const categories = ref([]);
        const tickets = ref([]);
        const activeCategoryId = ref(null); // 預設 null 為全部

        // 分頁相關狀態
        const total = ref(0);         // 總筆數
        const totalPages = ref(0);    // 總頁數
        const currentPage = ref(0);   // 當前頁碼 (第1頁是 0)
        const pageSize = ref(9);      // 每頁筆數

        // 讀取分類列表
        const loadCategories = async () => {
            try {
                const res = await fetch('api/tickets/categories');
                if (!res.ok) throw new Error('無法取得門票分類');
                const result = await res.json();
                if (result.status === 'success') {
                    categories.value = result.data;
                } else {
                    throw new Error(result.message || '無法取得門票分類');
                }
            } catch (err) {
                //console.error('讀取分類錯誤:', err);
            }
        };

        // 讀取門票商品列表資料
        const loadTickets = async (categoryId, pageNum = 0) => {
            try {
                let url = `api/tickets?page=${pageNum}&size=${pageSize.value}`;
                if (categoryId) {
                    url += `&categoryId=${categoryId}`;
                }
                const res = await fetch(url);
                if (!res.ok) throw new Error('讀取門票列表失敗');
                const result = await res.json();
                if (result.status === 'success') {
                    tickets.value = result.data.data;
                    total.value = result.data.total;
                    totalPages.value = result.data.totalPages;
                    currentPage.value = result.data.currentPage;
                } else {
                    throw new Error(result.message || '讀取門票列表失敗');
                }
            } catch (err) {
                //console.error('讀取門票錯誤:', err);
                alert(err.message || '載入門票列表失敗，請重新整理頁面');
            }
        };

        // 分類篩選
        const selectCategory = (id) => {
            activeCategoryId.value = id;
            loadTickets(id, 0); // 每次切換分類，都必須重設回第 1 頁 (0)
        };

        // 分頁切換
        const changePage = (pageNum) => {
            if (pageNum < 0 || pageNum >= totalPages.value) return;
            loadTickets(activeCategoryId.value, pageNum);
        };

        // 詳情頁
        const goToDetail = (id) => {
            location.href = `ticket/detail?ticketId=${id}`;
        };

        onMounted(() => {
            loadCategories();
            loadTickets(activeCategoryId.value, 0); // 進入畫面是第0頁

            // 滾動按鈕邏輯
            const categoryListContainer = document.getElementById('categoryListContainer');
            const leftBtn = document.getElementById('scrollLeftBtn');
            const rightBtn = document.getElementById('scrollRightBtn');

            if (categoryListContainer && leftBtn && rightBtn) {
                leftBtn.classList.remove('bound-hidden');
                rightBtn.classList.remove('bound-hidden');

                leftBtn.onclick = () => {
                    categoryListContainer.scrollBy({ left: -(categoryListContainer.clientWidth + 16), behavior: 'smooth' });
                };

                rightBtn.onclick = () => {
                    categoryListContainer.scrollBy({ left: (categoryListContainer.clientWidth + 16), behavior: 'smooth' });
                };
            }
        });

        // 提供給html
        return {
            categories,
            tickets,
            activeCategoryId,
            total,
            totalPages,
            currentPage,
            pageSize,
            selectCategory,
            changePage,
            goToDetail
        };
    }
}).mount('#app');
