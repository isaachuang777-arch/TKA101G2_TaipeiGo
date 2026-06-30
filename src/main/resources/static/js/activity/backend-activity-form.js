document.addEventListener("DOMContentLoaded", function () {
    initDragAndDrop();
    initTicketDynamicList();
    initExistingImages();
});

// ==========================================
// 現有圖片刪除邏輯
// ==========================================
function initExistingImages() {
    document.querySelectorAll('.existing-remove-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const imgId = this.getAttribute('data-img-id');
            // 建立一個 hidden input 記錄要刪除的 ID
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'deleteImageIds';
            hiddenInput.value = imgId;
            this.closest('form').appendChild(hiddenInput);
            
            // 將畫面上這張圖移除
            this.closest('.existing-image-item').remove();
            
            // 顯示提示文字
            const msg = document.getElementById('deleteMessage');
            if (msg) msg.style.display = 'inline-block';
        });
    });
}

// ==========================================
// 拖曳多圖上傳與預覽邏輯
// ==========================================
function initDragAndDrop() {
    const dropZone = document.getElementById('uploadDropZone');
    const fileInput = document.getElementById('upFiles');
    const previewContainer = document.getElementById('previewContainer');
    
    if (!dropZone || !fileInput || !previewContainer) return;

    // 用來儲存所有選取的檔案
    let selectedFiles = [];

    // 處理拖曳事件視覺回饋
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add('dragover');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove('dragover');
        }, false);
    });

    // 處理點擊觸發 file input (避免 event bubbling 造成連點閃退)
    dropZone.addEventListener('click', (e) => {
        if (e.target !== fileInput) {
            fileInput.click();
        }
    });

    // 處理檔案放置
    dropZone.addEventListener('drop', (e) => {
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFiles(files);
        }
    });

    // 處理點擊選取檔案
    fileInput.addEventListener('change', function () {
        handleFiles(this.files);
    });

    function handleFiles(files) {
        if (files.length === 0) return;

        // 將新選擇的檔案加入到陣列中
        Array.from(files).forEach(file => {
            if (file.type.match('image.*')) {
                selectedFiles.push(file);
            }
        });

        updateFileInput();
        renderPreviews();
    }

    // 更新 input[type="file"] 的 files (使用 DataTransfer)
    function updateFileInput() {
        const dt = new DataTransfer();
        selectedFiles.forEach(file => dt.items.add(file));
        fileInput.files = dt.files;
    }

    // 渲染預覽畫面
    function renderPreviews() {
        previewContainer.innerHTML = ''; // 清空重新渲染
        
        selectedFiles.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = function (e) {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                previewItem.innerHTML = `
                    <img src="${e.target.result}" alt="預覽圖">
                    <button type="button" class="remove-btn" title="移除此圖片" data-index="${index}">
                        <i class="bi bi-x-lg"></i>
                    </button>
                `;
                
                // 綁定移除單張圖片按鈕
                previewItem.querySelector('.remove-btn').addEventListener('click', function(event) {
                    event.stopPropagation(); // 避免觸發 dropZone 點擊
                    const idx = parseInt(this.getAttribute('data-index'));
                    selectedFiles.splice(idx, 1); // 移除該檔案
                    updateFileInput();
                    renderPreviews(); // 重新渲染
                });

                previewContainer.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        });
    }
}

// ==========================================
// 行程門票動態增刪邏輯
// ==========================================
function initTicketDynamicList() {
    const container = document.getElementById('ticketListContainer');
    const addBtn = document.getElementById('btnAddTicket');
    
    // 如果找不到，可能是不在此頁面
    if (!container || !addBtn) return;

    // 綁定新增按鈕
    addBtn.addEventListener('click', function() {
        addTicketItem();
    });

    // 綁定事件委派給移除按鈕
    container.addEventListener('click', function(e) {
        if (e.target.closest('.btn-remove-ticket')) {
            const item = e.target.closest('.ticket-item');
            if (container.querySelectorAll('.ticket-item').length <= 1) {
                alert('至少需要保留一個行程明細！');
                return;
            }
            item.remove();
            updateSequences();
        }
    });

    // 動態新增一筆明細
    function addTicketItem() {
        // 從頁面上隱藏的模板取得 HTML，或是直接用 JS 生成
        // 為了簡單，我們直接複製第一個選項的內容並重置
        const existingItems = container.querySelectorAll('.ticket-item');
        if (existingItems.length === 0) return;

        const firstItem = existingItems[0];
        const newItem = firstItem.cloneNode(true);
        
        // 重置 select 的值
        const select = newItem.querySelector('select');
        if (select) select.value = '';
        
        // 加入 DOM
        container.appendChild(newItem);
        updateSequences();
    }

    // 更新左側的序號數字
    function updateSequences() {
        const items = container.querySelectorAll('.ticket-item');
        items.forEach((item, index) => {
            const seqBadge = item.querySelector('.ticket-sequence');
            if (seqBadge) {
                seqBadge.textContent = (index + 1);
            }
        });
    }
}
