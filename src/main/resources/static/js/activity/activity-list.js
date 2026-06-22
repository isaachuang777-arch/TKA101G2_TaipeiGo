document.addEventListener('DOMContentLoaded', function () {

    // 一進網頁，立刻呼叫你的 @RestController 拿所有活動！
    fetchActivities();

    // 綁定 Header 上的搜尋按鈕事件
    const searchBtn = document.querySelector('.search-btn');
    if(searchBtn) {
        searchBtn.addEventListener('click', function() {
            fetchActivities();
        });
    }
    
    // 呼叫抓取分類 API
    fetchCategories();

});

// ==========================================
// API 呼叫與渲染邏輯 - 分類小卡
// ==========================================
function fetchCategories() {
    fetch('/activities/categories')
        .then(response => response.json())
        .then(data => {
            renderCategoryCards(data);
        })
        .catch(error => {
            console.error('抓取分類失敗:', error);
            const container = document.getElementById('categoryListContainer');
            if(container) container.innerHTML = '<p>暫無分類資料</p>';
        });
}

function renderCategoryCards(categories) {
    const container = document.getElementById('categoryListContainer');
    if (!container) return;
    
    container.innerHTML = ''; // 清空載入中文字
    
    if (!categories || categories.length === 0) {
        container.innerHTML = '<p>尚無分類</p>';
        return;
    }
    
    // 為了實作真正的「無縫無限輪播」，我們把原始分類複製 10 份！
    // 這樣 DOM 裡面會有足夠的卡片可以一直往右滑
    const clonedCategories = [];
    for (let i = 0; i < 10; i++) {
        clonedCategories.push(...categories);
    }
    
    clonedCategories.forEach(cate => {
        // 處理 Base64 圖片，如果沒有就用預設色塊
        const imgSrc = cate.cateIcon ? `data:image/jpeg;base64,${cate.cateIcon}` : '/images/activity/default.png';
        
        const cardHTML = `
            <div class="category-card" onclick="alert('你點擊了：${cate.cateName}，未來可以串接篩選功能！')">
                <div class="category-img-wrapper">
                    <img src="${imgSrc}" alt="${cate.cateName}" onerror="this.src='/images/activity/default.png'">
                </div>
                <div class="category-info">
                    <h3 class="category-name">${cate.cateName}</h3>
                    <p class="category-count">探索活動 ></p>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', cardHTML);
    });
    
    // 初始化左右滑動按鈕邏輯，並把「原始長度」傳進去計算
    setTimeout(() => {
        initCategoryScroll(categories.length);
    }, 100);
}

function initCategoryScroll(originalCount) {
    const container = document.getElementById('categoryListContainer');
    const leftBtn = document.getElementById('scrollLeftBtn');
    const rightBtn = document.getElementById('scrollRightBtn');
    if(!container || !leftBtn || !rightBtn) return;

    // 永遠不隱藏左右按鈕
    leftBtn.classList.remove('bound-hidden');
    rightBtn.classList.remove('bound-hidden');

    const cardElements = container.querySelectorAll('.category-card');
    if (cardElements.length < originalCount * 10) return;

    // 計算「一整組」原始卡片在畫面上的真實像素寬度 (包含間距 gap)
    const singleSetWidth = cardElements[originalCount].offsetLeft - cardElements[0].offsetLeft;

    // 偷偷把卷軸初始位置，設定在第 5 組的開頭 (正中間)
    // 這樣使用者一開始不管往左還是往右，都有滿滿 4 組卡片可以滑
    container.style.scrollBehavior = 'auto'; // 確保不會有平滑動畫干擾
    container.scrollLeft = singleSetWidth * 5;

    leftBtn.onclick = () => {
        container.scrollBy({ left: -(container.clientWidth + 16), behavior: 'smooth' });
    };

    rightBtn.onclick = () => {
        container.scrollBy({ left: (container.clientWidth + 16), behavior: 'smooth' });
    };

    // 終極魔法：無縫時空跳躍
    // 當使用者真的很有毅力滑到極限邊緣時，偷偷把卷軸拉回正中間
    container.addEventListener('scroll', () => {
        // 如果向右滑到了第 8 組
        if (container.scrollLeft >= singleSetWidth * 8) {
            const offset = container.scrollLeft - (singleSetWidth * 8);
            container.style.scrollBehavior = 'auto';
            container.scrollLeft = (singleSetWidth * 5) + offset; // 保持小數點誤差，完美無縫接軌
        } 
        // 如果向左滑到了第 2 組
        else if (container.scrollLeft <= singleSetWidth * 2) {
            const offset = (singleSetWidth * 2) - container.scrollLeft;
            container.style.scrollBehavior = 'auto';
            container.scrollLeft = (singleSetWidth * 5) - offset;
        }
    });
}

// ==========================================
// API 呼叫與渲染邏輯 - 活動網格
// ==========================================
function fetchActivities() {
    // 收集 Header 搜尋列的字
    const searchInput = document.querySelector('.search-input');
    const keyword = searchInput ? searchInput.value : '';

    // 組合你的萬用查詢 URL (對應到 ActivityController 的 @GetMapping)
    let url = new URL(window.location.origin + '/activities');

    if (keyword) url.searchParams.append('activityName', keyword);

    console.log("正在請求資料: " + url);

    // 發送請求給你的 @RestController
    fetch(url)
        .then(response => response.json())
        .then(data => {
            renderCards(data); // 拿到資料後，交給畫卡片的函數
        })
        .catch(error => {
            console.error('取得活動資料失敗:', error);
            document.getElementById('activityListContainer').innerHTML = '<p>讀取失敗，請稍後再試。</p>';
        });
}

// ==========================================
// 2. 動態把 JSON 畫成漂亮的卡片
// ==========================================
function renderCards(activityList) {
    const container = document.getElementById('activityListContainer');

    if (!activityList || activityList.length === 0) {
        container.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; color: #666;">找不到符合條件的活動，請嘗試其他關鍵字。</p>';
        return;
    }

    // 清空原本的「資料載入中...」
    container.innerHTML = '';

    // 把陣列裡的每一個 activityVO，變成一張 HTML 卡片
    activityList.forEach(activity => {
        // 從資料庫串接真實圖片 (取第一張)，如果沒有就用預設色塊
        let imageUrl = '/images/default-activity.jpg';
        if (activity.activityImage && activity.activityImage.length > 0) {
            let originalSrc = activity.activityImage[0].activityImageSrc; // e.g., /images/activity/activity_1.png
            let parts = originalSrc.split('/');
            let filename = parts.pop();
            
            // 把副檔名去掉，改抓 thumb_ 開頭的 .jpg 小圖
            let baseName = filename.substring(0, filename.lastIndexOf('.'));
            if (!baseName) baseName = filename; 
            
            imageUrl = '/images/activity/thumb_' + baseName + '.jpg';
        }

        // 未來這裡的 href 要改成 /activity/詳情頁的ID
        const cardHtml = `
            <a href="#" class="activity-card">
                <div class="card-img-placeholder" style="background-image: url('${imageUrl}')"></div>
                <div class="card-content">
                    <h3 class="card-title">${activity.activityName}</h3>
                    <p class="card-desc">${activity.activityDesc || '這是一個超讚的台北體驗行程！'}</p>
                    
                    <div class="card-footer">
                        <div>
                            <span class="card-discount-tag">查看詳情</span>
                        </div>
                        <div class="card-price">特價 TWD ${activity.finalPrice || '0'}</div>
                    </div>
                </div>
            </a>
        `;

        // 把畫好的卡片塞進網格裡
        container.insertAdjacentHTML('beforeend', cardHtml);
    });
}
