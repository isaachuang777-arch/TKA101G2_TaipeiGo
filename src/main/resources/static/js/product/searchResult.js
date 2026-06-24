document.addEventListener('DOMContentLoaded', function () {
    // 1. 從網址列抓取 keyword (例如 ?keyword=動物園)
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword') || '';

    // 把關鍵字顯示在左上角
    const summaryText = document.getElementById('searchSummary');
    if (keyword) {
        summaryText.innerHTML = `為您找到與「<b>${keyword}</b>」相關的體驗`;
    } else {
        summaryText.innerHTML = `探索全部體驗`;
    }

    // 2. 準備呼叫 API
    fetchSearchResults(keyword);

    // 2.5 左側側邊欄關鍵字搜尋 (SPA 體驗)
    const sidebarSearchInput = document.getElementById('sidebarSearchInput');
    if (sidebarSearchInput) {
        sidebarSearchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault(); // 防止預設行為
                const newKeyword = this.value.trim();
                
                // 1. 更新瀏覽器的 URL，這樣重新整理時搜尋字詞不會不見 (不換頁)
                const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?keyword=' + encodeURIComponent(newKeyword);
                window.history.pushState({path: newUrl}, '', newUrl);

                // 2. 更新上方顯示的文字
                const summaryText = document.getElementById('searchSummary');
                if (newKeyword) {
                    summaryText.innerHTML = `為您找到與「<b>${newKeyword}</b>」相關的體驗`;
                } else {
                    summaryText.innerHTML = `探索全部體驗`;
                }

                // 3. 重新打後端 API 撈新資料 (這會自動重繪畫面跟更新拉桿極值)
                fetchSearchResults(newKeyword);
            }
        });
    }

    // 3. 綁定頂部分類頁籤切換事件
    const tabItems = document.querySelectorAll('.tab-item');
    tabItems.forEach(tab => {
        tab.addEventListener('click', function () {
            // 移除所有 tab 的 active 狀態
            tabItems.forEach(t => t.classList.remove('active'));
            // 為點擊的 tab 加上 active
            this.classList.add('active');
            
            // 更新全域變數並觸發過濾
            currentType = this.getAttribute('data-type');
            filterAndRenderCards();
        });
    });

    // 4. 綁定「套用篩選」按鈕
    const applyFilterBtn = document.getElementById('applyFilterBtn');
    if (applyFilterBtn) {
        applyFilterBtn.addEventListener('click', filterAndRenderCards);
    }

    // 5. 初始化價格雙拉桿 (noUiSlider)
    const priceSlider = document.getElementById('priceSlider');
    const minPriceInput = document.getElementById('minPrice');
    const maxPriceInput = document.getElementById('maxPrice');

    if (priceSlider) {
        noUiSlider.create(priceSlider, {
            start: [0, 5000],
            connect: true,
            step: 50,
            range: {
                'min': 0,
                'max': 10000
            },
            format: {
                to: value => Math.round(value),
                from: value => Math.round(value)
            }
        });

        // 當拉桿被拖曳時，自動更新輸入框的數字
        priceSlider.noUiSlider.on('update', function (values, handle) {
            if (handle === 0) {
                minPriceInput.value = values[0];
            } else {
                maxPriceInput.value = values[1];
            }
        });

        // 當拉桿放開 (拖曳結束) 時，我們不自動過濾，等待使用者按下「套用篩選」
    }

    // 當輸入框手動輸入數字時，同步更新拉桿位置 (但不自動過濾)
    [minPriceInput, maxPriceInput].forEach(input => {
        input.addEventListener('change', function () {
            priceSlider.noUiSlider.set([minPriceInput.value, maxPriceInput.value]);
        });
    });

    // 6. 綁定排序選單 (通常排序會直接生效，不需按鈕)
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', filterAndRenderCards);
    }

    // 7. 分類 Checkbox 不綁定自動過濾，等待使用者按下「套用篩選」

});

// 全域變數，用來存放原始資料 (為了之後左側面板篩選用)
let allData = [];
// 預設選擇「全部」
let currentType = 'ALL';

// ==========================================
// 前端篩選邏輯 (Client-Side Filtering)
// ==========================================
function filterAndRenderCards() {
    // 1. (已由頂部分類頁籤控制 currentType)

    // 2. 抓取價格區間
    const minPriceInput = document.getElementById('minPrice').value;
    const maxPriceInput = document.getElementById('maxPrice').value;
    const minPrice = minPriceInput ? parseInt(minPriceInput) : 0;
    const maxPrice = maxPriceInput ? parseInt(maxPriceInput) : 9999999;

    // 3. 抓取被勾選的主題標籤
    const checkedTags = Array.from(document.querySelectorAll('.tag-checkbox:checked'))
                             .map(cb => parseInt(cb.value));

    // 4. 針對 allData 進行前端多重過濾
    const filteredData = allData.filter(item => {
        // [條件A] 過濾類型 (門票 / 活動 / 全部)
        if (currentType !== 'ALL' && item.type !== currentType) return false;

        // [條件B] 過濾價格
        if (item.price < minPrice || item.price > maxPrice) return false;

        // [條件C] 過濾標籤 (如果有勾選標籤的話)
        if (checkedTags.length > 0) {
            // 如果該商品沒有任何分類，就直接過濾掉
            if (!item.categoryIds || item.categoryIds.length === 0) return false;
            
            // 判斷該商品擁有的分類，是否與「被打勾的標籤」有交集
            const hasMatchedTag = item.categoryIds.some(id => checkedTags.includes(id));
            if (!hasMatchedTag) return false;
        }

        return true; // 三個條件都過，就保留這張卡片！
    });

    // 5. 處理排序 (價格高低 / 最推薦)
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        const sortValue = sortSelect.value;
        if (sortValue === 'priceAsc') {
            filteredData.sort((a, b) => a.price - b.price);
        } else if (sortValue === 'priceDesc') {
            filteredData.sort((a, b) => b.price - a.price);
        }
        // 如果是 'relevance' (最推薦)，就保持原樣，因為原本就是依照關鍵字命中順序 (後端返回的預設順序)
    }

    // 6. 拿過濾且排序後的資料去重新畫畫面
    renderCards(filteredData);
}

// ==========================================
// 打 API 拿資料的非同步函式
// ==========================================
async function fetchSearchResults(keyword) {
    try {
        // 呼叫你的 SearchController API
        const response = await fetch(`/api/search?keyword=${encodeURIComponent(keyword)}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 拿到熱騰騰的 JSON！
        allData = await response.json();

        // 根據抓回來的資料，動態更新雙拉桿的最大/最小值
        if (allData.length > 0) {
            const prices = allData.map(item => item.price);
            const minP = Math.min(...prices);
            const maxP = Math.max(...prices);
            
            const priceSlider = document.getElementById('priceSlider');
            if (priceSlider && priceSlider.noUiSlider) {
                priceSlider.noUiSlider.updateOptions({
                    range: {
                        'min': Math.max(0, minP - 100), // 留點緩衝
                        'max': maxP + 100
                    }
                });
                // 順便重置拉桿位置到極值
                priceSlider.noUiSlider.set([Math.max(0, minP - 100), maxP + 100]);
            }
        }

        // 丟給前端過濾邏輯去處理 (因為一開始有預設選中的類型)
        filterAndRenderCards();

    } catch (error) {
        console.error("搜尋發生錯誤:", error);
        document.getElementById('searchResultsContainer').innerHTML = '<h3 style="color:red;">系統連線異常，請稍後再試。</h3>';
    }
}

// ==========================================
// 動態畫出 HTML 卡片的函式 (CSR 的靈魂)
// ==========================================
function renderCards(dataList) {
    const container = document.getElementById('searchResultsContainer');
    const noResultsBlock = document.getElementById('noResultsBlock');

    // 1. 先把原本的「骨架屏」清空
    container.innerHTML = '';

    // 2. 防呆：如果沒有資料，顯示「找不到結果」區塊
    if (!dataList || dataList.length === 0) {
        noResultsBlock.style.display = 'block';
        return;
    }

    // 有資料就隱藏防呆區塊
    noResultsBlock.style.display = 'none';

    // 3. 跑迴圈，把每一筆 DTO 畫成卡片
    let htmlContent = '';

    dataList.forEach(item => {

        // 【防呆】處理圖片邏輯
        let finalImageUrl = item.imageUrl;
        if (!finalImageUrl || finalImageUrl.trim() === '') {
            finalImageUrl = 'https://placehold.co/600x400/eeeeee/999999?text=Taipei+Go';
        }

        // 統一顯示「查看詳情」
        const typeLabel = '查看詳情';
        const typeColor = item.type === 'TICKET' ? '#007bff' : '#ff5b00';
        
        // 左上角標籤
        const badgeText = item.type === 'TICKET' ? '門票' : '活動';
        const dotColor = item.type === 'TICKET' ? '#007bff' : '#ff5b00';
        
        // 動態生成前往詳細頁面的網址與參數名稱
        const linkUrl = item.type === 'TICKET' 
            ? `/ticket/detail?ticketId=${item.id}` 
            : `/activity/detail?activityId=${item.id}`;

        // 完美對齊你的 activity 前端顯示方式 (使用 activity-card 結構)
        htmlContent += `
            <a href="${linkUrl}" class="activity-card" style="position: relative;">
                <!-- 左上角商品類型標籤 -->
                <div class="card-type-badge">
                    <span class="badge-dot" style="background-color: ${dotColor};"></span>
                    ${badgeText}
                </div>

                <div class="card-img-placeholder" style="background-image: url('${finalImageUrl}')"></div>
                <div class="card-content">
                    <h3 class="card-title">${item.title}</h3>
                    <p class="card-desc">${item.description || '探索這趟美好的旅程！'}</p>
                    <div class="card-footer">
                        <div>
                            <!-- 直接使用 activity-list.css 原生的淡橘色方塊，或者若不要方塊只要字，這裡先移除行內樣式，並改為純字體顏色 -->
                            <span class="card-discount-tag" style="background: transparent; border: none; padding: 0; color: #ff5b00; font-size: 14px; font-weight: bold;">
                                ${typeLabel}
                            </span>
                        </div>
                        <div class="card-price">TWD ${item.price}</div>
                    </div>
                </div>
            </a>
        `;
    });

    // 4. 一次性把組合好的 HTML 塞進去網頁！
    container.innerHTML = htmlContent;
}
