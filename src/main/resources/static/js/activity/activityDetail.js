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
    
    if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
        const pendingAction = {
            action: 'favorite',
            activityId: typeof activityId !== 'undefined' ? activityId : null
        };
        sessionStorage.setItem('pendingActivityAction', JSON.stringify(pendingAction));
        
        const currentUrl = window.location.pathname + window.location.search;
        window.location.href = contextPath + "auth/login?redirect=" + encodeURIComponent(currentUrl);
        return;
    }
    
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

    // =============== 新增：檢查庫存以控制按鈕狀態 ===============
    const addToCartBtn = document.getElementById('addToCartBtn');
    const bookingBtn = document.getElementById('bookingBtn');
    
    // 如果這兩個按鈕存在（不在預覽模式），才需要檢查
    if (addToCartBtn && bookingBtn) {
        if (totalQty > 0) {
            // 發送請求問後端庫存夠不夠
            fetch(`/activities/checkStock?activityId=${activityId}&quantity=${totalQty}`)
                .then(res => res.json())
                .then(hasStock => {
                    if (hasStock) {
                        // 庫存足夠，恢復按鈕
                        addToCartBtn.style.pointerEvents = 'auto';
                        addToCartBtn.style.backgroundColor = '#fff';
                        addToCartBtn.style.color = '#ff5b00';
                        addToCartBtn.style.borderColor = '#ff5b00';
                        addToCartBtn.innerText = '加入購物車';

                        bookingBtn.style.pointerEvents = 'auto';
                        bookingBtn.style.backgroundColor = '#ff5b00';
                        bookingBtn.style.color = '#fff';
                        bookingBtn.style.borderColor = '#ff5b00';
                        bookingBtn.innerText = '立即購買';
                    } else {
                        // 庫存不足，按鈕反灰
                        addToCartBtn.style.pointerEvents = 'none';
                        addToCartBtn.style.backgroundColor = '#f1f5f9';
                        addToCartBtn.style.color = '#94a3b8';
                        addToCartBtn.style.borderColor = '#cbd5e1';
                        addToCartBtn.innerText = '目前庫存不足';

                        bookingBtn.style.pointerEvents = 'none';
                        bookingBtn.style.backgroundColor = '#cbd5e1';
                        bookingBtn.style.color = '#fff';
                        bookingBtn.style.borderColor = '#cbd5e1';
                        bookingBtn.innerText = '目前庫存不足';
                    }
                })
                .catch(err => {
                    console.error("庫存檢查失敗:", err);
                });
        } else {
            // 數量為 0 時，恢復預設狀態
            addToCartBtn.style.pointerEvents = 'auto';
            addToCartBtn.style.backgroundColor = '#fff';
            addToCartBtn.style.color = '#ff5b00';
            addToCartBtn.style.borderColor = '#ff5b00';
            addToCartBtn.innerText = '加入購物車';

            bookingBtn.style.pointerEvents = 'auto';
            bookingBtn.style.backgroundColor = '#ff5b00';
            bookingBtn.style.color = '#fff';
            bookingBtn.style.borderColor = '#ff5b00';
            bookingBtn.innerText = '立即購買';
        }
    }
    // =========================================================
}

// 點擊預訂與購物車時的防呆驗證
function validateBooking(event, action = 'cart', isAutoTrigger = false) {
    const errorMsgSpan = document.getElementById('bookingErrorMsg');
    if (errorMsgSpan) {
        // 先重置透明度，但不清空文字，避免排版跳動
        errorMsgSpan.style.opacity = '0';
    }

    const total = parseInt(document.getElementById('totalTicketsCount').innerText) || 0;
    if (total === 0) {
        if (!isAutoTrigger) {
            if (errorMsgSpan) {
                errorMsgSpan.style.color = '#ff4d4f';
                errorMsgSpan.innerText = "請至少選擇一個方案並增加數量";
                errorMsgSpan.style.opacity = '1';
            } else {
                alert("請至少選擇一個方案並增加數量！");
            }
        }
        return false;
    }
    
    if (!selectedDateParam) {
        if (!isAutoTrigger) {
            if (errorMsgSpan) {
                errorMsgSpan.style.color = '#ff4d4f';
                errorMsgSpan.innerText = "請選擇預計啟用日期";
                errorMsgSpan.style.opacity = '1';
            } else {
                alert("請選擇預計啟用日期！");
            }
        }
        return false;
    }

    if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
        const pendingAction = {
            action: action,
            activityId: typeof activityId !== 'undefined' ? activityId : null,
            selectedDateParam: selectedDateParam,
            qtyAdult: document.getElementById('qtyAdult') ? document.getElementById('qtyAdult').value : 0,
            qtyChild: document.getElementById('qtyChild') ? document.getElementById('qtyChild').value : 0,
            qtyConcession: document.getElementById('qtyConcession') ? document.getElementById('qtyConcession').value : 0
        };
        sessionStorage.setItem('pendingActivityAction', JSON.stringify(pendingAction));
        
        const currentUrl = window.location.pathname + window.location.search;
        window.location.href = contextPath + "auth/login?redirect=" + encodeURIComponent(currentUrl);
        return false;
    }

    // 這裡我們要把選擇的票種，逐一發送給後端加入購物車
    const specs = [
        { type: "ADULT", qty: parseInt(document.getElementById('qtyAdult') ? document.getElementById('qtyAdult').value : 0) || 0 },
        { type: "CHILD", qty: parseInt(document.getElementById('qtyChild') ? document.getElementById('qtyChild').value : 0) || 0 },
        { type: "CONCESSION", qty: parseInt(document.getElementById('qtyConcession') ? document.getElementById('qtyConcession').value : 0) || 0 }
    ];

    let successCount = 0;
    
    // 透過 Promise.all 確保所有發送都完成
    const promises = specs
        .filter(specInfo => specInfo.qty > 0)
        .map(specInfo => {
            const cartVO = {
                productId: activityId,
                productType: 'ACTIVITY', // 告訴後端這是活動
                productQuantity: specInfo.qty,
                spec: specInfo.type, // ADULT, CHILD, 或 CONCESSION
                // Spring Boot 的 LocalDateTime 通常需要 ISO 格式的字串 (YYYY-MM-DDTHH:mm:ss)
                expiryDate: selectedDateParam + "T00:00:00" 
            };

            return fetch(contextPath + 'frontend/cart/insertCart', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(cartVO)
            }).then(response => {
                if (response.ok) {
                    successCount++;
                } else {
                    // 已移除 console.error 錯誤日誌
                }
            });
        });

    Promise.all(promises).then(() => {
        if (successCount > 0) {
            if (action === 'buyNow') {
                window.location.href = contextPath + "frontend/cart/shoppingCart"; 
            } else {
                if (errorMsgSpan) {
                    errorMsgSpan.style.color = '#10b981'; // 綠色
                    errorMsgSpan.innerText = "已將選取的方案加入購物車";
                    errorMsgSpan.style.opacity = '1';
                    
                    // 3 秒後自動隱藏成功訊息
                    setTimeout(() => {
                        errorMsgSpan.style.opacity = '0';
                        // 等待動畫結束後清空文字 (0.8s = 800ms)
                        setTimeout(() => {
                            if (errorMsgSpan.style.opacity === '0') {
                                errorMsgSpan.innerText = '';
                            }
                        }, 800);
                    }, 3000);
                } else {
                    alert(`已將選取的方案加入購物車`);
                }
                
                if (typeof loadCartCount === 'function') {
                    loadCartCount();
                }
            }
        } else {
            if (errorMsgSpan) {
                errorMsgSpan.style.color = '#ff4d4f';
                errorMsgSpan.innerText = "加入購物車時發生錯誤，請稍後再試！";
                errorMsgSpan.style.opacity = '1';
            } else {
                alert("加入購物車時發生錯誤，請稍後再試！");
            }
        }
    }).catch(error => {
        console.error("發生錯誤:", error);
        alert("網路錯誤，無法加入購物車");
    });

    return false; // 暫時防止 a 連結跳轉
}

// 自動執行登入前的未完成操作
window.addEventListener('DOMContentLoaded', () => {
    if (typeof isUserLoggedIn !== 'undefined' && isUserLoggedIn) {
        const pendingStr = sessionStorage.getItem('pendingActivityAction');
        if (pendingStr) {
            try {
                const pendingAction = JSON.parse(pendingStr);
                
                // 確認這真的是同一個活動，以免跑到別的活動頁面觸發
                if (pendingAction.activityId === (typeof activityId !== 'undefined' ? activityId : null)) {
                    
                    if (pendingAction.action === 'favorite') {
                        const favBtn = document.querySelector('.favorite-btn-klook');
                        if (favBtn) {
                            toggleFavorite(new Event('click'), favBtn);
                        }
                    } 
                    else if (pendingAction.action === 'booking' || pendingAction.action === 'cart' || pendingAction.action === 'buyNow') {
                        // 恢復狀態
                        selectedDateParam = pendingAction.selectedDateParam;
                        if (document.getElementById('openCalendarBtn')) {
                            document.getElementById('openCalendarBtn').innerText = selectedDateParam;
                        }
                        
                        if (document.getElementById('qtyAdult')) document.getElementById('qtyAdult').value = pendingAction.qtyAdult;
                        if (document.getElementById('qtyChild')) document.getElementById('qtyChild').value = pendingAction.qtyChild;
                        if (document.getElementById('qtyConcession')) document.getElementById('qtyConcession').value = pendingAction.qtyConcession;
                        
                        updateTotalAndPrice();
                        
                        // 延遲一點點執行，確保畫面已經更新完畢
                        setTimeout(() => {
                            validateBooking(new Event('click'), pendingAction.action === 'booking' ? 'cart' : pendingAction.action, true);
                        }, 500);
                    }
                }
            } catch (e) {
                console.error("解析 pendingActivityAction 失敗", e);
            } finally {
                // 不管成功失敗都清掉，避免無限循環
                sessionStorage.removeItem('pendingActivityAction');
            }
        }
    }
});
