document.addEventListener('DOMContentLoaded', function () {

    // 一進網頁，立刻呼叫你的 @RestController 拿所有活動！
    fetchActivities();

    // 綁定 Header 上的搜尋按鈕事件
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function () {
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
            if (container) container.innerHTML = '<p>暫無分類資料</p>';
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
    if (!container || !leftBtn || !rightBtn) return;

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
// 1. API 呼叫與邏輯分流
// ==========================================
function fetchActivities() {
    const searchInput = document.querySelector('.search-input');
    const keyword = searchInput ? searchInput.value : '';

    if (keyword) {
        // 【情境 A：有輸入關鍵字】走原本的搜尋邏輯 (顯示全部符合的網格)
        let url = new URL(window.location.origin + '/activities');
        url.searchParams.append('keyword', keyword);

        fetch(url)
            .then(res => res.json())
            .then(data => renderCards(data))
            .catch(console.error);
    } else {
        // 【情境 B：沒有關鍵字的首頁】走我們剛做好的主題區塊 API
        let url = new URL(window.location.origin + '/activities/home-sections');

        fetch(url)
            .then(res => res.json())
            .then(data => renderSections(data))
            .catch(console.error);
    }
}

// ==========================================
// 2. 渲染：主題區塊模式 (H2 + 3張卡片)
// ==========================================
function renderSections(sections) {
    const container = document.getElementById('activityListContainer');
    container.innerHTML = '';
    // 移除外層網格樣式，因為現在是一層一層的區塊
    container.classList.remove('activity-grid');

    if (!sections || sections.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #666; margin-top: 40px;">目前沒有精選活動。</p>';
        return;
    }

    // 迴圈跑每一個主題區塊
    sections.forEach(section => {
        // 外層 div
        const sectionDiv = document.createElement('div');
        sectionDiv.className = 'theme-section';
        sectionDiv.style.marginBottom = '40px'; // 區塊之間留白

        // 依據標題決定對應的 FontAwesome 圖示
        let iconHtml = '<i class="fa-solid fa-fire" style="color: #ff5722; margin-right: 8px;"></i>';
        if (section.categoryName.includes('最佳優惠')) {
            iconHtml = '<i class="fa-solid fa-tags" style="color: #ff5722; margin-right: 8px;"></i>';
        } else if (section.categoryName.includes('懶得規劃')) {
            iconHtml = '<i class="fa-solid fa-compass" style="color: #ff5722; margin-right: 8px;"></i>';
        }

        // 加入 H2 標題與 Icon
        sectionDiv.innerHTML = `<h2 style="font-size: 24px; font-weight: 800; margin-bottom: 20px; color: #333; display: flex; align-items: center;">${iconHtml} ${section.categoryName}</h2>`;

        // 內層網格裝卡片 (直接沿用你寫好的 CSS 網格系統！)
        const gridDiv = document.createElement('div');
        gridDiv.className = 'activity-grid';

        // 迴圈把這 3 張卡片畫進去
        section.activities.forEach(activity => {
            const cardHtml = buildCardHtml(activity);
            gridDiv.insertAdjacentHTML('beforeend', cardHtml);
        });

        sectionDiv.appendChild(gridDiv);
        container.appendChild(sectionDiv);
    });
}

// ==========================================
// 3. 渲染：傳統網格模式 (用於搜尋結果)
// ==========================================
function renderCards(activityList) {
    const container = document.getElementById('activityListContainer');
    container.innerHTML = '';
    // 確保外層有網格樣式
    container.classList.add('activity-grid');

    if (!activityList || activityList.length === 0) {
        container.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; color: #666;">找不到符合條件的活動，請嘗試其他關鍵字。</p>';
        return;
    }

    activityList.forEach(activity => {
        const cardHtml = buildCardHtml(activity);
        container.insertAdjacentHTML('beforeend', cardHtml);
    });
}

// 共用的組裝卡片 HTML 函數 (避免程式碼重複)
function buildCardHtml(activity) {
    let imageUrl = '/images/default-activity.jpg';
    if (activity.activityImage && activity.activityImage.length > 0) {
        let originalSrc = activity.activityImage[0].activityImageSrc;
        let parts = originalSrc.split('/');
        let filename = parts.pop();
        let baseName = filename.substring(0, filename.lastIndexOf('.'));
        if (!baseName) baseName = filename;
        imageUrl = '/images/activity/thumb_' + baseName + '.jpg';
    }

    return `
        <a href="/activity/detail?activityId=${activity.activityId}" class="activity-card">
            <div class="card-img-placeholder" style="background-image: url('${imageUrl}')"></div>
            <div class="card-content">
                <h3 class="card-title">${activity.activityName}</h3>
                <p class="card-desc">${activity.activityDesc || '這是一個超讚的體驗行程！'}</p>
                <div class="card-footer">
                    <div><span class="card-discount-tag">查看詳情</span></div>
                    <div class="card-price">特價 TWD ${activity.adultPrice || '0'}</div>
                </div>
            </div>
        </a>
    `;
}
