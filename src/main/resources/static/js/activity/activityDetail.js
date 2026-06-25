// activityDetail.js

let slideIndex = 0;
let selectedDateParam = "";
let fpInstance = null;
let activityId = ""; 
let currentActivity = null;

// ==========================================
// 1. 初始化頁面與獲取資料 (CSR)
// ==========================================
document.addEventListener("DOMContentLoaded", function() {
    // 從網址列讀取 activityId (例如 /activity/detail?activityId=5)
    const urlParams = new URLSearchParams(window.location.search);
    activityId = urlParams.get('activityId');

    if (!activityId) {
        alert("找不到此活動！將返回活動清單。");
        window.location.href = "/activity";
        return;
    }

    // 預設設定「明日」日期
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    selectedDateParam = formatDateStr(tomorrowDate);

    // 動態設定「後天」與「大後天」的按鈕文字 (MM月DD日)
    const dayAfter = new Date();
    dayAfter.setDate(dayAfter.getDate() + 2);
    document.getElementById('btnDayAfter').innerText = `${dayAfter.getMonth() + 1}月${dayAfter.getDate()}日`;

    const threeDays = new Date();
    threeDays.setDate(threeDays.getDate() + 3);
    document.getElementById('btnThreeDays').innerText = `${threeDays.getMonth() + 1}月${threeDays.getDate()}日`;

    // 呼叫 RestController 獲取活動資料
    fetch(`/activities?activityId=${activityId}`)
        .then(res => res.json())
        .then(data => {
            if (!data || data.length === 0) {
                alert("找不到此活動資料！");
                window.location.href = "/activity";
                return;
            }
            currentActivity = data[0];
            renderActivityData(currentActivity);
            initFlatpickr(); // 資料載入完畢後初始化日曆
        })
        .catch(err => {
            console.error("獲取資料失敗:", err);
            document.getElementById("breadcrumbName").innerText = "載入失敗";
        });
});

// ==========================================
// 2. 將 JSON 資料填入 HTML (取代 Thymeleaf)
// ==========================================
function renderActivityData(activity) {
    // 麵包屑與標題
    document.getElementById("breadcrumbName").innerText = activity.activityName;
    document.getElementById("activityTitle").innerText = activity.activityName;
    
    // 最低價 (取成人票價作為基準展示)
    document.getElementById("mainPriceDisplay").innerText = activity.adultPrice || 0;

    // 行程介紹 (純文字，保留換行)
    document.getElementById("activityDesc").innerText = activity.activityDesc || "暫無介紹";

    // 票價
    document.getElementById("priceAdult").innerText = activity.adultPrice || 0;
    document.getElementById("qtyAdult").setAttribute("data-price", activity.adultPrice || 0);

    document.getElementById("priceChild").innerText = activity.childPrice || 0;
    document.getElementById("qtyChild").setAttribute("data-price", activity.childPrice || 0);

    document.getElementById("priceConcession").innerText = activity.concessionPrice || 0;
    document.getElementById("qtyConcession").setAttribute("data-price", activity.concessionPrice || 0);

    // 處理圖片 (如果沒有圖片，給一張預設圖)
    let images = [];
    if (activity.activityImage && activity.activityImage.length > 0) {
        images = activity.activityImage;
    } else {
        images = [{ activityImageSrc: "/images/activity/default.png" }];
    }

    renderGalleryAndLightbox(images);
}

function renderGalleryAndLightbox(images) {
    const gallerySection = document.getElementById("gallerySectionContainer");
    const lightboxContainer = document.getElementById("lightboxContainer");

    // --- 渲染 Gallery 網格 ---
    let mainImgHtml = `
        <div class="main-image has-img" onclick="openLightbox(0)">
            <img src="${images[0].activityImageSrc}" alt="主圖">
        </div>
    `;

    let subImagesHtml = '<div class="sub-images">';
    for (let i = 1; i <= 4; i++) {
        let hasImg = i < images.length;
        subImagesHtml += `
            <div class="sub-image-box ${hasImg ? 'has-img' : 'fallback'}" ${hasImg ? `onclick="openLightbox(${i})"` : ''}>
                ${hasImg ? `<img src="${images[i].activityImageSrc}" alt="副圖">` : '<div class="no-image-placeholder-sub">暫無圖片</div>'}
                ${i === 4 && images.length > 0 ? '<div class="view-photos-btn" onclick="event.stopPropagation(); openLightbox(0)">查看照片</div>' : ''}
            </div>
        `;
    }
    subImagesHtml += '</div>';
    gallerySection.innerHTML = mainImgHtml + subImagesHtml;

    // --- 渲染 Lightbox 燈箱 ---
    let slidesHtml = '<div class="lightbox-main">';
    let thumbsHtml = '<div class="lightbox-thumbnails">';

    images.forEach((img, index) => {
        slidesHtml += `
            <div class="lightbox-slide">
                <img src="${img.activityImageSrc}">
                <div class="lightbox-counter">${index + 1} / ${images.length}</div>
            </div>
        `;
        thumbsHtml += `
            <div class="thumb-item" onclick="currentSlide(${index})">
                <img src="${img.activityImageSrc}">
            </div>
        `;
    });

    slidesHtml += `
        <a class="prev-arrow" onclick="changeSlide(-1)">&#10094;</a>
        <a class="next-arrow" onclick="changeSlide(1)">&#10095;</a>
    </div>`;
    thumbsHtml += '</div>';

    lightboxContainer.innerHTML = slidesHtml + thumbsHtml;
}


// ==========================================
// 圖片預覽區 (完全同 ticketDetail)
// ==========================================
function openLightbox(index) {
    document.getElementById("imageLightbox").style.display = "flex";
    currentSlide(index);
}

function closeLightbox() {
    document.getElementById("imageLightbox").style.display = "none";
}

function changeSlide(n) {
    showSlides(slideIndex += n);
}

function currentSlide(index) {
    showSlides(slideIndex = index);
}

function showSlides(n) {
    let slides = document.getElementsByClassName("lightbox-slide");
    let thumbs = document.getElementsByClassName("thumb-item");
    
    if (slides.length === 0) return;

    if (n >= slides.length) { slideIndex = 0; }
    if (n < 0) { slideIndex = slides.length - 1; }
    
    for (let i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }
    for (let i = 0; i < thumbs.length; i++) {
        thumbs[i].className = thumbs[i].className.replace(" active-thumb", "");
    }
    
    if(slides[slideIndex]) slides[slideIndex].style.display = "block";
    if(thumbs[thumbs.length - 1] && slideIndex >= thumbs.length) {
    } else if (thumbs[slideIndex]) {
        thumbs[slideIndex].className += " active-thumb";
    }
}

// 加入我的最愛
function toggleFavorite(event, button) {
    event.stopPropagation();
    
    button.classList.toggle('active');
    const icon = button.querySelector('i');
    
    if (button.classList.contains('active')) {
        icon.classList.replace('fa-regular', 'fa-solid');
    } else {
        icon.classList.replace('fa-solid', 'fa-regular');
    }
}

// 格式化日期成 YYYY-MM-DD
function formatDateStr(date) {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

// 清除日期快選按鈕的 active 狀態
function clearQuickDateActive() {
    document.querySelectorAll('.date-selector-row .date-btn:not(#openCalendarBtn)').forEach(btn => {
        btn.classList.remove('active');
    });
}

// 日期快選按鈕的切換效果
function selectQuickDate(type) {
    clearQuickDateActive();
    if (event && event.target) {
        event.target.classList.add('active');
    }
    
    if (fpInstance) {
        fpInstance.clear();
        document.getElementById("openCalendarBtn").innerText = "所有日期";
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

    selectedDateParam = formatDateStr(targetDate);
}

// 初始化 Flatpickr 日曆
function initFlatpickr() {
    fpInstance = flatpickr("#inlineDatePicker", {
        locale: "zh_tw",
        minDate: "today",       
        dateFormat: "Y-m-d",
        disableMobile: "true",  
        static: true,           
        appendTo: document.querySelector(".calendar-wrapper"), 
        
        onChange: function(selectedDates, dateStr, instance) {
            if (selectedDates.length > 0) {
                document.getElementById("openCalendarBtn").innerText = dateStr;
                selectedDateParam = dateStr;
                
                clearQuickDateActive();
            }
        }
    });

    const openCalBtn = document.getElementById("openCalendarBtn");
    if (openCalBtn) {
        openCalBtn.addEventListener("click", function(e) {
            e.stopPropagation(); 
            fpInstance.open();
        });
    }
}

// 數量加減器
function changeQty(button, amount) {
    const input = button.parentElement.querySelector('.qty-input');
    let currentVal = parseInt(input.value) || 0;
    currentVal += amount;
    
    if (currentVal < 0) currentVal = 0;
    if (currentVal > 99) currentVal = 99;
    
    input.value = currentVal;
    
    updateTotalAndPrice();
}

// 同步計算「總張數」與「總金額」並更新畫面
function updateTotalAndPrice() {
    let totalQty = 0;
    let totalPrice = 0;
    
    document.querySelectorAll('.qty-input').forEach(input => {
        const qty = parseInt(input.value) || 0;
        const price = parseInt(input.getAttribute('data-price')) || 0;
        
        totalQty += qty;
        totalPrice += (qty * price);
    });
    
    document.getElementById('totalTicketsCount').innerText = totalQty;
    document.getElementById('totalPriceAmount').innerText = totalPrice.toLocaleString();
}

// 點擊預訂與購物車時的防呆驗證
function validateBooking(event) {
    const total = parseInt(document.getElementById('totalTicketsCount').innerText) || 0;
    if (total === 0) {
        alert("請至少選擇一個方案並增加數量！");
        return false;
    }
    
    if (!selectedDateParam) {
        alert("請選擇預計啟用日期！");
        return false;
    }

    // 未來此處串接加入購物車 API
    alert(`成功！\n活動 ID：${activityId}\n日期：${selectedDateParam}\n總票數：${total}張`);
    return false; // 暫時防止 a 連結跳轉
}
