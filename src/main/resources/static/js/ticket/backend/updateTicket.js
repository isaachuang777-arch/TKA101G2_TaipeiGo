/**
 * updateTicket.html 專用多圖控管與硬刪除聯動腳本
 */

// 用來記錄所有被點擊刪除的舊圖片 ID 陣列
let deletedImageIds = [];

/**
 * 點擊現有圖片的紅叉叉
 */
function markImageForDeletion(imageId) {
    if (confirm("確定要移除這張現有圖片嗎？")) {
        
        // 將圖片 ID 塞入準備刪除的名單中
        if (!deletedImageIds.includes(imageId)) {
            deletedImageIds.push(imageId);
        }
        
        // 讓前端畫面的這張圖有淡出動畫，然後移除網頁元素
        const wrapper = document.getElementById('old-img-wrapper-' + imageId);
        if (wrapper) {
            wrapper.style.transition = "all 0.3s ease";
            wrapper.style.opacity = "0";
            wrapper.style.transform = "scale(0.7)";
            setTimeout(() => {
                wrapper.remove();
                // 舊圖在畫面上被刪除後，即時去更新與重新計算總數
                updateUploadCountText();
            }, 300);
        }
        
        // 更新動態 Hidden Inputs，確保按下 Submit 送出表單時能順利帶回正確的 Java 參數 
        updateDeleteInputsContainer();
    }
}

/**
 * 2. 動態生成隱藏的 Input 標籤，隨表單一起 POST 回後台
 */
function updateDeleteInputsContainer() {
    const container = document.getElementById('deleteImageIdsContainer');
    if (!container) return;
    container.innerHTML = ''; // 先清空，重新產生最新的 hidden 標籤
    
    deletedImageIds.forEach(id => {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'deleteImageIds'; // 完美對接 Controller 臨時變數接收名
        hiddenInput.value = id;
        container.appendChild(hiddenInput);
    });
}

/**
 *  計算目前的圖片總量
 */
function updateUploadCountText() {
    const currentOldCount = document.querySelectorAll('[id^="old-img-wrapper-"]').length;
    const previewContainer = document.getElementById('previewContainer');
    const currentNewCount = previewContainer ? previewContainer.querySelectorAll('.preview-img-wrapper').length : 0;
    
    const totalCount = currentOldCount + currentNewCount;
    
    const countTextEl = document.getElementById('uploadCountText');
    if (countTextEl) {
        countTextEl.innerText = `(目前 ${totalCount}/8 張)`;
    }
    
    return 8 - totalCount;
}

/**
 * 計算剩餘張數
 */
window.getAvailableImageQuota = function() {
    const currentOldCount = document.querySelectorAll('[id^="old-img-wrapper-"]').length;
    const previewContainer = document.getElementById('previewContainer');
    // 扣除 "舊圖" 與 "已存在的新預覽圖" 之後，算出真正還可以再上傳多少個檔案
    const currentNewCount = previewContainer ? previewContainer.querySelectorAll('.preview-img-wrapper').length : 0;
    
    return 8 - (currentOldCount + currentNewCount);
};

// 確保該頁面圖片數量正確
document.addEventListener("DOMContentLoaded", function() {
    const fileInputEl = document.getElementById('ticketImageFiles');
    if (fileInputEl) {
        fileInputEl.addEventListener('change', function(e) {
            const files = this.files;
            if (!files || files.length === 0) return;
            
            // 抓取當前最即時的空位額度
            const allowedQuota = window.getAvailableImageQuota();
            
            // 如果這次想塞的數量，大於當前剩下的空位額度
            if (files.length > allowedQuota) {
                alert(`選取失敗！商品總圖片數上限為 8 張。\n\n目前現有保留與新預覽總計：${8 - allowedQuota} 張\n您本次最多只能再選取 ${allowedQuota} 張新圖片。`);
                
                this.value = ''; // 當場清空
                
                e.stopImmediatePropagation(); 
                e.preventDefault();
                return false;
            }
        }, true); // 使用 true (事件捕獲機制) 確保比 addTicket.js 先查
    }

    // 頁面第一次載入時先算一次
    updateUploadCountText();

    // 啟動動態 MutationObserver 監視器
    const previewContainer = document.getElementById('previewContainer');
    if (previewContainer) {
        const observer = new MutationObserver(function() {
            setTimeout(() => {
                updateUploadCountText();
            }, 10);
        });
        observer.observe(previewContainer, { childList: true });
    }
});