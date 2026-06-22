// ticketDetail.js

let slideIndex = 0;
let selectedDateParam = "";
let fpInstance = null;
let ticketId = ""; // 存放從 HTML 讀取的 ticketId

// 圖片預覽區
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
    const textSpan = button.querySelector('span');
    
    if (button.classList.contains('active')) {
        icon.classList.replace('fa-regular', 'fa-solid');
        // console.log("商品已加入收藏");
        // TODO: 串接加入我的最愛API

    } else {
        icon.classList.replace('fa-solid', 'fa-regular');
        // console.log("商品已取消收藏");
        // TODO: 串接取消加入我的最愛API
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
    document.querySelectorAll('.date-selector-row .date-btn:not(.btn-all-dates)').forEach(btn => {
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
    updateBookingUrl();
}

// 門票數量加減器
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
    
    updateBookingUrl();
}

// 動態網址參數更新 (不使用 Thymeleaf 行內語法，改用讀取到的 ticketId)
function updateBookingUrl() {
    const bookingBtn = document.getElementById("bookingBtn");
    const addToCartBtn = document.getElementById("addToCartBtn");
    
    if (!bookingBtn || !addToCartBtn) return;
    
    let baseBookingHref = bookingBtn.getAttribute("href").split('?')[0] + "?ticketId=" + ticketId;
    let baseCartHref = addToCartBtn.getAttribute("href").split('?')[0] + "?ticketId=" + ticketId;
    
    if (selectedDateParam) {
        baseBookingHref += "&date=" + selectedDateParam;
        baseCartHref += "&date=" + selectedDateParam;
    }
    
    const qtyInputs = document.querySelectorAll('.qty-input');
    const adultQty = qtyInputs[0] ? qtyInputs[0].value : 0;
    const childQty = qtyInputs[1] ? qtyInputs[1].value : 0;
    const concessionQty = qtyInputs[2] ? qtyInputs[2].value : 0;
    
    const qtyParams = `&adultQty=${adultQty}&childQty=${childQty}&concessionQty=${concessionQty}`;
    
    bookingBtn.setAttribute("href", baseBookingHref + qtyParams);
    addToCartBtn.setAttribute("href", baseCartHref + qtyParams);
}

// 點擊預訂與購物車時的防呆驗證
function validateBooking(event) {
    const total = parseInt(document.getElementById('totalTicketsCount').innerText) || 0;
    if (total === 0) {
        alert("請至少選擇一個方案並增加數量！");
        return false;
    }
    return true;
}

// 初始化頁面事件
document.addEventListener("DOMContentLoaded", function() {
    // 從 HTML 節點獲取 Thymeleaf 帶過來的 ticketId
    ticketId = document.body.getAttribute("data-ticket-id") || "";

    // 預設設定「明日」日期
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    selectedDateParam = formatDateStr(tomorrowDate);
    updateBookingUrl();

    // 初始化 Flatpickr 日曆
    fpInstance = flatpickr("#inlineDatePicker", {
        locale: "zh_tw",
        minDate: "today",       
        dateFormat: "Y-m-d",
        disableMobile: "true",  
        static: true,           
        appendTo: document.querySelector(".calendar-wrapper"), 
        
        onChange: function(selectedDates, dateStr, instance) {
            if (selectedDates.length > 0) {
                document.getElementById("openCalendarBtn").innerText =   dateStr;
                selectedDateParam = dateStr;
                
                clearQuickDateActive();
                updateBookingUrl();
            }
        }
    });

    // 日曆開啟事件
    const openCalBtn = document.getElementById("openCalendarBtn");
    if (openCalBtn) {
        openCalBtn.addEventListener("click", function(e) {
            e.stopPropagation(); 
            fpInstance.open();
        });
    }

    // TODO: 加入購物車 => 按下"加入購物車" btn後要先確認該門票是否還有庫存可以使用

    // TODO: 立即購買 => 按下"立即購買"btn 後要先確認該門票是否還有庫存可以使用
});