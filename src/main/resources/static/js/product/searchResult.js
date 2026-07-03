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

    // 2. 初始載入第一頁
    filterAndRenderCards(0);

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

                // 3. 重新發送搜尋請求 (從第 0 頁開始)
                filterAndRenderCards(0);
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
            filterAndRenderCards(0);
        });
    });

    // 4. 綁定「套用篩選」按鈕
    const applyFilterBtn = document.getElementById('applyFilterBtn');
    if (applyFilterBtn) {
        applyFilterBtn.addEventListener('click', () => filterAndRenderCards(0));
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
    }

    // 當輸入框手動輸入數字時，同步更新拉桿位置
    [minPriceInput, maxPriceInput].forEach(input => {
        if(input) {
            input.addEventListener('change', function () {
                if(priceSlider && priceSlider.noUiSlider) {
                    priceSlider.noUiSlider.set([minPriceInput.value, maxPriceInput.value]);
                }
            });
        }
    });

    // 6. 綁定排序選單
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', () => filterAndRenderCards(0));
    }

});

// 全域變數
let currentType = 'ALL';
let currentPage = 0;
const PAGE_SIZE = 12;

// ==========================================
// 統整條件並打 API (真實後端分頁與過濾)
// ==========================================
function filterAndRenderCards(page = 0) {
    currentPage = page;
    
    const minPriceInput = document.getElementById('minPrice');
    const maxPriceInput = document.getElementById('maxPrice');
    const minPrice = minPriceInput && minPriceInput.value ? parseInt(minPriceInput.value) : 0;
    const maxPrice = maxPriceInput && maxPriceInput.value ? parseInt(maxPriceInput.value) : 9999999;
    
    // 抓取被勾選的主題標籤
    const checkedTags = Array.from(document.querySelectorAll('.tag-checkbox:checked')).map(cb => parseInt(cb.value));
    
    const sortSelect = document.getElementById('sortSelect');
    const sortBy = sortSelect ? sortSelect.value : 'relevance';
    
    // 抓取關鍵字 (優先從輸入框取得最新的值)
    const sidebarSearchInput = document.getElementById('sidebarSearchInput');
    let keyword = '';
    if (sidebarSearchInput && sidebarSearchInput.value !== undefined) {
        keyword = sidebarSearchInput.value.trim();
    } else {
        const urlParams = new URLSearchParams(window.location.search);
        keyword = urlParams.get('keyword') || '';
    }

    // 更新瀏覽器 URL 狀態 (確保重新整理不會遺失)
    const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?keyword=' + encodeURIComponent(keyword);
    window.history.replaceState({path: newUrl}, '', newUrl);

    // 更新上方顯示的文字
    const summaryText = document.getElementById('searchSummary');
    if (summaryText) {
        if (keyword) {
            summaryText.innerHTML = `為您找到與「<b>${keyword}</b>」相關的體驗`;
        } else {
            summaryText.innerHTML = `探索全部體驗`;
        }
    }

    // 建立 API URL
    let url = `/api/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${PAGE_SIZE}&type=${currentType}&sortBy=${sortBy}`;
    
    if (minPrice > 0) url += `&minPrice=${minPrice}`;
    if (maxPrice < 9999999) url += `&maxPrice=${maxPrice}`;
    
    if (checkedTags.length > 0) {
        // 將陣列轉成逗號分隔的字串傳給後端
        url += `&categoryIds=${checkedTags.join(',')}`;
    }
    
    fetchSearchResults(url);
}

// ==========================================
// 打 API 拿資料的非同步函式
// ==========================================
async function fetchSearchResults(url) {
    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 拿到後端的 Page 物件 (包含 content 與 totalPages)
        const pageData = await response.json();
        
        // 更新價格雙拉桿的最高範圍 (依據過濾後的最高價格)
        if (pageData.maxPrice !== undefined) {
            const priceSlider = document.getElementById('priceSlider');
            if (priceSlider && priceSlider.noUiSlider) {
                const currentMax = priceSlider.noUiSlider.options.range.max;
                // 為了避免過度縮小，我們讓 maxPrice 永遠不會低於 1000
                const newMax = Math.max(1000, pageData.maxPrice + 100); 
                
                priceSlider.noUiSlider.updateOptions({
                    range: {
                        'min': 0,
                        'max': newMax
                    }
                });
            }
        }

        // 1. 渲染卡片 (取出 pageData.content)
        renderCards(pageData.content);
        
        // 2. 渲染分頁按鈕 (傳入總頁數與當前頁)
        renderPagination(pageData.totalPages, currentPage);

    } catch (error) {
        console.error("搜尋發生錯誤:", error);
        document.getElementById('searchResultsContainer').innerHTML = '<h3 style="color:red;">系統連線異常，請稍後再試。</h3>';
    }
}

// ==========================================
// 動態產生分頁按鈕
// ==========================================
function renderPagination(totalPages, currentPage) {
    const parentContainer = document.getElementById('searchResultsContainer').parentElement;
    
    // 清除舊的分頁區塊
    const oldPagination = document.getElementById('searchPaginationWrapper');
    if (oldPagination) oldPagination.remove();

    // 只有 1 頁或 0 頁就不顯示分頁列
    if (totalPages <= 1) return;

    const paginationWrapper = document.createElement('div');
    paginationWrapper.id = 'searchPaginationWrapper';
    paginationWrapper.style = 'display: flex; justify-content: center; align-items: center; gap: 10px; margin-top: 30px; margin-bottom: 50px; width: 100%;';

    // 上一頁按鈕
    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
    prevBtn.style = 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 12px; border-radius: 4px; cursor: pointer;';
    if (currentPage > 0) {
        prevBtn.onclick = () => { 
            filterAndRenderCards(currentPage - 1); 
            window.scrollTo({ top: 300, behavior: 'smooth' }); 
        };
    } else {
        prevBtn.style.opacity = '0.5';
        prevBtn.style.cursor = 'not-allowed';
    }
    paginationWrapper.appendChild(prevBtn);

    // 數字頁碼按鈕
    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1; // 顯示給使用者看的是從 1 開始
        btn.style = (i === currentPage) 
            ? 'background-color: #ff5722; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold;' 
            : 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 16px; border-radius: 4px; cursor: pointer;';
        
        btn.onclick = () => {
            filterAndRenderCards(i);
            window.scrollTo({ top: 300, behavior: 'smooth' });
        };
        paginationWrapper.appendChild(btn);
    }
    
    // 下一頁按鈕
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
    nextBtn.style = 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 12px; border-radius: 4px; cursor: pointer;';
    if (currentPage < totalPages - 1) {
        nextBtn.onclick = () => { 
            filterAndRenderCards(currentPage + 1); 
            window.scrollTo({ top: 300, behavior: 'smooth' }); 
        };
    } else {
        nextBtn.style.opacity = '0.5';
        nextBtn.style.cursor = 'not-allowed';
    }
    paginationWrapper.appendChild(nextBtn);
    
    // 把分頁列塞進卡片區塊的外部下方
    parentContainer.appendChild(paginationWrapper);
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
            finalImageUrl = '/images/activity/default-placeholder.svg';
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

                <div class="card-img-placeholder" style="background-image: url('${finalImageUrl}'), url('/images/activity/default-placeholder.svg'); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>
                <div class="card-content">
                    <h3 class="card-title">${item.title}</h3>
                    <p class="card-desc">${item.description || '探索這趟美好的旅程！'}</p>
                      <div class="card-footer" style="display: flex; justify-content: space-between; align-items: flex-end;">
                          <div>
                              <span class="card-discount-tag" style="background: transparent; border: none; padding: 0; color: #ff5b00; font-size: 14px; font-weight: bold;">
                                  ${typeLabel}
                              </span>
                          </div>
                          <div class="price-wrapper" style="text-align: right; display: flex; flex-direction: column; line-height: 1.2;">
                              <span style="font-size: 12px; color: #999999; text-decoration: line-through; margin-bottom: 2px;">NT$ ${item.originalPrice || Math.round(item.price * 1.25)}</span>
                              <span style="font-size: 14px; color: #333333;">NT$ <strong style="font-size: 20px; color: #ff5722; margin-left: 2px;">${item.price}</strong> 起</span>
                          </div>
                      </div>
                </div>
            </a>
        `;
    });

    // 4. 一次性把組合好的 HTML 塞進去網頁！
    container.innerHTML = htmlContent;
}
