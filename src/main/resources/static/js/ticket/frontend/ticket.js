const { createApp, ref, onMounted } = Vue;

createApp({
    setup() {
        const categories = ref([]);
        const tickets = ref([]);
        const activeCategoryId = ref(null); // 預設 null 為全部

        // 讀取分類列表
        const loadCategories = async () => {
            try {
                const res = await fetch('api/tickets/categories');
                if (!res.ok) throw new Error('Network response was not ok');
                categories.value = await res.json();
            } catch (err) {
                // console.error('讀取分類錯誤:', err);
            }
        };

        // 讀取門票商品列表資料
        const loadTickets = async (categoryId) => {
            try {
                let url = 'api/tickets';
                if (categoryId) {
                    url += `?categoryId=${categoryId}`;
                }
                const res = await fetch(url);
                if (!res.ok) throw new Error('Network response was not ok');
                tickets.value = await res.json();
            } catch (err) {
                // console.error('讀取門票錯誤:', err);
            }
        };

        // 分類篩選
        const selectCategory = (id) => {
            activeCategoryId.value = id;
            loadTickets(id);
        };

        // 詳情頁
        const goToDetail = (id) => {
            location.href = `ticket/detail?ticketId=${id}`;
        };


        onMounted(() => {
            loadCategories();
            loadTickets();

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
            selectCategory,
            goToDetail
        };
    }
}).mount('#app');
