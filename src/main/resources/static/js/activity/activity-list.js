document.addEventListener('DOMContentLoaded', function () {
    fetchActivities();

    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function () {
            fetchActivities();
        });
    }

    fetchCategories();
});

let currentCateId = null;
let currentCateName = '所有活動';
const PAGE_SIZE = 9;

function fetchCategories() {
    fetch('/activities/categories')
        .then(response => response.json())
        .then(data => {
            renderCategoryCards(data);
        })
        .catch(error => {
            console.error('取得分類失敗:', error);
            const container = document.getElementById('categoryListContainer');
            if (container) container.innerHTML = '<p>暫無分類資料</p>';
        });
}

function renderCategoryCards(categories) {
    const container = document.getElementById('categoryListContainer');
    if (!container) return;

    container.innerHTML = ''; 

    if (!categories || categories.length === 0) {
        const catSection = document.querySelector('.category-section');
        if(catSection) catSection.style.display = 'none';
        return;
    }

    // 新增「全部活動」的魔法小卡在最前面
    const allActivityCate = {
        activityCateId: '',
        cateName: '全部活動',
        cateIcon: null
    };
    categories.unshift(allActivityCate);

    const clonedCategories = [];
    for (let i = 0; i < 10; i++) {
        clonedCategories.push(...categories);
    }

    clonedCategories.forEach(cate => {
        const imgSrc = cate.cateIcon ? 'data:image/jpeg;base64,' + cate.cateIcon : '/images/activity/default.png';

        const cardHTML = `
            <div class="category-card" onclick="loadActivitiesByCategory('${cate.activityCateId}', '${cate.cateName}')">
                <div class="category-img-wrapper">
                    <img src="${imgSrc}" alt="${cate.cateName}" onerror="this.onerror=null; this.src='/images/activity/default-placeholder.svg';">
                </div>
                <div class="category-info">
                    <h3 class="category-name">${cate.cateName}</h3>
                    <p class="category-count">探索活動 ></p>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', cardHTML);
    });

    setTimeout(() => {
        initCategoryScroll(categories.length);
    }, 100);
}

function initCategoryScroll(originalCount) {
    const container = document.getElementById('categoryListContainer');
    const leftBtn = document.getElementById('scrollLeftBtn');
    const rightBtn = document.getElementById('scrollRightBtn');
    if (!container || !leftBtn || !rightBtn) return;

    leftBtn.classList.remove('bound-hidden');
    rightBtn.classList.remove('bound-hidden');

    const cardElements = container.querySelectorAll('.category-card');
    if (cardElements.length < originalCount * 10) return;

    const singleSetWidth = cardElements[originalCount].offsetLeft - cardElements[0].offsetLeft;
    container.style.scrollBehavior = 'auto'; 
    container.scrollLeft = singleSetWidth * 5;

    leftBtn.onclick = () => {
        container.scrollBy({ left: -(container.clientWidth + 16), behavior: 'smooth' });
    };

    rightBtn.onclick = () => {
        container.scrollBy({ left: (container.clientWidth + 16), behavior: 'smooth' });
    };

    container.addEventListener('scroll', () => {
        if (container.scrollLeft >= singleSetWidth * 8) {
            const offset = container.scrollLeft - (singleSetWidth * 8);
            container.style.scrollBehavior = 'auto';
            container.scrollLeft = (singleSetWidth * 5) + offset;
        }
        else if (container.scrollLeft <= singleSetWidth * 2) {
            const offset = (singleSetWidth * 2) - container.scrollLeft;
            container.style.scrollBehavior = 'auto';
            container.scrollLeft = (singleSetWidth * 5) - offset;
        }
    });
}

function loadActivitiesByCategory(cateId, cateName) {
    currentCateId = cateId;
    currentCateName = cateName;
    fetchPaginatedActivities(1);
    window.scrollTo({ top: 400, behavior: 'smooth' });
}

function fetchActivities() {
    const searchInput = document.querySelector('.search-input');
    const keyword = searchInput ? searchInput.value : '';

    if (keyword) {
        let url = new URL(window.location.origin + '/activities');
        url.searchParams.append('keyword', keyword);

        fetch(url)
            .then(res => res.json())
            .then(data => {
                currentCateId = null;
                currentCateName = '搜尋結果';
                renderSearchCards(data);
            })
            .catch(console.error);
    } else {
        currentCateId = null;
        currentCateName = '所有活動';
        fetchPaginatedActivities(1);
    }
}

function buildCardHtml(activity) {
    let imageUrl = '/images/activity/default-placeholder.svg';
    if (activity.activityImage && activity.activityImage.length > 0) {
        imageUrl = activity.activityImage[0].activityImageSrc;
    }

    const currentPrice = activity.adultPrice || 0;
    // 原價現在從後端的 adultOriginalPrice 取出
    const originalPrice = activity.adultOriginalPrice || Math.round(currentPrice * 1.25);

    return `
        <a href="/activity/detail?activityId=${activity.activityId}" class="activity-card">
            <div class="card-img-placeholder" style="background-image: url('${imageUrl}'), url('/images/activity/default-placeholder.svg'); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>
            <div class="card-content">
                <h3 class="card-title">${activity.activityName}</h3>
                <p class="card-desc">${activity.activityDesc || '這是一個很讚的體驗行程！'}</p>
                <div class="card-footer" style="display: flex; justify-content: space-between; align-items: flex-end;">
                    <div><span class="card-discount-tag">查看詳情</span></div>
                    <div class="price-wrapper" style="text-align: right; display: flex; flex-direction: column; line-height: 1.2;">
                        <span style="font-size: 12px; color: #999999; text-decoration: line-through; margin-bottom: 2px;">NT$ ${originalPrice}</span>
                        <span style="font-size: 14px; color: #333333;">NT$ <strong style="font-size: 20px; color: #ff5722; margin-left: 2px;">${currentPrice}</strong> 起</span>
                    </div>
                </div>
            </div>
        </a>
    `;
}

function renderSearchCards(activityList) {
    const container = document.getElementById('activityListContainer');
    container.innerHTML = '';
    container.classList.remove('activity-grid');
    
    const sectionDiv = document.createElement('div');
    sectionDiv.className = 'theme-section';
    sectionDiv.style.marginBottom = '40px';
    sectionDiv.innerHTML = `<h2 style="font-size: 24px; font-weight: 800; margin-bottom: 20px; color: #333; display: flex; align-items: center;"><i class="fa-solid fa-magnifying-glass" style="color: #ff5722; margin-right: 8px;"></i> 搜尋結果</h2>`;
    
    if (!activityList || activityList.length === 0) {
        sectionDiv.insertAdjacentHTML('beforeend', '<p style="text-align: center; color: #666;">沒有符合條件的活動，請嘗試其他關鍵字。</p>');
        container.appendChild(sectionDiv);
        return;
    }
    
    const gridDiv = document.createElement('div');
    gridDiv.className = 'activity-grid';
    activityList.forEach(activity => {
        gridDiv.insertAdjacentHTML('beforeend', buildCardHtml(activity));
    });
    
    sectionDiv.appendChild(gridDiv);
    container.appendChild(sectionDiv);
}

function fetchPaginatedActivities(page) {
    const container = document.getElementById('activityListContainer');
    container.classList.remove('activity-grid');

    let url = `/activities?page=${page}&pageSize=${PAGE_SIZE}`;
    if (currentCateId) {
        url += `&cateId=${currentCateId}`;
    }

    fetch(url)
        .then(res => res.json())
        .then(activities => {
            container.innerHTML = ''; 
            
            const sectionDiv = document.createElement('div');
            sectionDiv.className = 'theme-section';
            sectionDiv.style.marginBottom = '40px'; 
            
            let iconHtml = '<i class="fa-solid fa-list" style="color: #ff5722; margin-right: 12px; font-size: 30px;"></i>';
            const titleHtml = `<h2 class="category-section-title" style="align-items: center;">${iconHtml} ${currentCateName}</h2>`;
            sectionDiv.innerHTML = titleHtml;

            if (!activities || activities.length === 0) {
                sectionDiv.insertAdjacentHTML('beforeend', '<p style="text-align: center; color: #666; margin-top: 40px;">目前沒有活動。</p>');
                container.appendChild(sectionDiv);
                
                const oldPagination = document.getElementById('paginationWrapper');
                if(oldPagination) oldPagination.remove();
                return;
            }

            const gridDiv = document.createElement('div');
            gridDiv.className = 'activity-grid';
            
            activities.forEach(act => {
                gridDiv.insertAdjacentHTML('beforeend', buildCardHtml(act));
            });
            
            sectionDiv.appendChild(gridDiv);
            container.appendChild(sectionDiv);
            
            fetchTotalPages(page);
        });
}

function fetchTotalPages(currentPage) {
    let url = `/activities/total-pages?pageSize=${PAGE_SIZE}`;
    if (currentCateId) {
        url += `&cateId=${currentCateId}`;
    }

    fetch(url)
        .then(res => res.text()) 
        .then(totalPages => {
            const total = parseInt(totalPages);
            
            const oldPagination = document.getElementById('paginationWrapper');
            if(oldPagination) oldPagination.remove();

            if(total <= 1) return; 

            const paginationWrapper = document.createElement('div');
            paginationWrapper.id = 'paginationWrapper';
            paginationWrapper.style = 'display: flex; justify-content: center; align-items: center; gap: 10px; margin-top: 30px; margin-bottom: 50px; width: 100%;';

            const prevBtn = document.createElement('button');
            prevBtn.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
            prevBtn.style = 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 12px; border-radius: 4px; cursor: pointer;';
            if (currentPage > 1) {
                prevBtn.onclick = () => { fetchPaginatedActivities(currentPage - 1); window.scrollTo({ top: 400, behavior: 'smooth' }); };
            } else {
                prevBtn.style.opacity = '0.5';
                prevBtn.style.cursor = 'not-allowed';
            }
            paginationWrapper.appendChild(prevBtn);

            for (let i = 1; i <= total; i++) {
                const btn = document.createElement('button');
                btn.innerText = i;
                btn.style = i === currentPage 
                    ? 'background-color: #ff5722; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold;' 
                    : 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 16px; border-radius: 4px; cursor: pointer;';
                
                btn.onclick = () => {
                    fetchPaginatedActivities(i);
                    window.scrollTo({ top: 400, behavior: 'smooth' });
                };
                paginationWrapper.appendChild(btn);
            }
            
            const nextBtn = document.createElement('button');
            nextBtn.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
            nextBtn.style = 'background-color: white; color: #333; border: 1px solid #ccc; padding: 8px 12px; border-radius: 4px; cursor: pointer;';
            if (currentPage < total) {
                nextBtn.onclick = () => { fetchPaginatedActivities(currentPage + 1); window.scrollTo({ top: 400, behavior: 'smooth' }); };
            } else {
                nextBtn.style.opacity = '0.5';
                nextBtn.style.cursor = 'not-allowed';
            }
            paginationWrapper.appendChild(nextBtn);
            
            document.getElementById('activityListContainer').appendChild(paginationWrapper);
        });
}
