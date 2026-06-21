let uploadedFiles = [];
const MAX_IMAGES = 8;

// 處理多圖上傳
async function previewImages(input) {
    if (!input.files || input.files.length === 0) return;

    // 檢查加入新圖後是否會超過 8 張上限
    if (uploadedFiles.length + input.files.length > MAX_IMAGES) {
        alert(`圖片數量超過上限！最多只能上傳 ${MAX_IMAGES} 張圖片，您目前已選 ${uploadedFiles.length} 張。`);
        input.value = ''; 
        return;
    }

    const newFiles = Array.from(input.files);
    const container = document.getElementById('previewContainer');

    for (const file of newFiles) {
        const dataUrl = await readFileAsDataURL(file);
        uploadedFiles.push(file);
        // 抓取物件當前在陣列的最新位置，並直接掛到舊圖尾端
        const currentIndex = uploadedFiles.length - 1; 
        const previewElement = createPreviewDOM(file, dataUrl, currentIndex);
        container.appendChild(previewElement);
    }

    updateUploadCount();
    input.value = ''; 
}

// 將 FileReader 包裝成 Promise 的同步序列
function readFileAsDataURL(file) {
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.readAsDataURL(file);
    });
}


function createPreviewDOM(file, src, index) {
    const wrapper = document.createElement('div');
    wrapper.className = 'preview-img-wrapper shadow-sm';

    const img = document.createElement('img');
    img.src = src;

    // 建立紅色刪除按鈕
    const deleteBtn = document.createElement('button');
    deleteBtn.type = 'button';
    deleteBtn.className = 'btn-delete-img';
    deleteBtn.innerHTML = '<i class="bi bi-x"></i>';
    deleteBtn.title = '刪除此圖片';

    // 點擊刪除按鈕事件
    deleteBtn.onclick = function() {
        const realIndex = uploadedFiles.indexOf(file);
        if (realIndex !== -1) {
            uploadedFiles.splice(realIndex, 1);
            wrapper.remove();
            updateUploadCount();
        }
    };

    wrapper.appendChild(img);
    wrapper.appendChild(deleteBtn);
    return wrapper;
}

// 更新圖片數量
function updateUploadCount() {
    const wrappers = document.querySelectorAll('.preview-img-wrapper');
    wrappers.forEach((wrapper, newIndex) => {
        wrapper.setAttribute('data-index', newIndex);
    });
    document.getElementById('uploadCountText').innerText = `(目前 ${uploadedFiles.length}/${MAX_IMAGES} 張)`;
}

// 表單送出攔截：利用 DataTransfer 打包陣列檔案至隱藏 input
document.getElementById('ticketForm').addEventListener('submit', function(e) {
    const dataTransfer = new DataTransfer();
    uploadedFiles.forEach(file => {
        dataTransfer.items.add(file);
    });

    let hiddenInput = document.getElementById('finalTicketImages');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.type = 'file';
        hiddenInput.id = 'finalTicketImages';
        hiddenInput.name = 'ticketImageFiles'; // 需與後端 Java Controller 接收的 MultipartFile 參數名一致
        hiddenInput.className = 'd-none';
        hiddenInput.multiple = true;
        this.appendChild(hiddenInput);
    }
    hiddenInput.files = dataTransfer.files;
});

// 清空
function resetForm() {
    if(confirm("確定要重設表單內容與清除所有已選圖片嗎？")) {
        document.getElementById('ticketForm').reset();
        uploadedFiles = [];
        document.getElementById('previewContainer').innerHTML = '';
        document.getElementById('uploadCountText').innerText = `(目前 0/${MAX_IMAGES} 張)`;
    }
}
