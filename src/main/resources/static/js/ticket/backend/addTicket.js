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

// 表單送出攔截與驗證
document.getElementById('ticketForm').addEventListener('submit', function(e) {
    let hasError = false;
    let firstErrorElement = null;

    function setError(inputEl, errorElId) {
        inputEl.classList.add('is-invalid');
        const errEl = document.getElementById(errorElId);
        if (errEl) {
            errEl.style.display = 'flex';
        }
        hasError = true;
        if (!firstErrorElement) {
            firstErrorElement = inputEl;
        }
    }

    // 1. 門票名稱驗證
    const ticketName = document.getElementById('ticketName');
    if (ticketName) {
        if (!ticketName.value || ticketName.value.trim() === '') {
            setError(ticketName, 'ticketNameError');
        } else if (ticketName.value.length > 50) {
            setError(ticketName, 'ticketNameError');
        }
    }

    // 2. 門票分類驗證 (至少選一個)
    const categoryCheckboxes = document.querySelectorAll('input[name="ticketCategories"]:checked');
    const categoryCard = document.querySelector('input[name="ticketCategories"]')?.closest('.card');
    if (categoryCheckboxes.length === 0) {
        if (categoryCard) {
            categoryCard.classList.add('border-danger');
        }
        const errEl = document.getElementById('ticketCategoriesError');
        if (errEl) {
            errEl.style.display = 'flex';
        }
        hasError = true;
        if (!firstErrorElement && categoryCard) {
            firstErrorElement = categoryCard;
        }
    }

    // 3. 景點地址驗證
    const ticketAddress = document.getElementById('ticketAddress');
    if (ticketAddress) {
        if (!ticketAddress.value || ticketAddress.value.trim() === '') {
            setError(ticketAddress, 'ticketAddressError');
        } else if (ticketAddress.value.length > 50) {
            setError(ticketAddress, 'ticketAddressError');
        }
    }

    // 4. 描述驗證
    const ticketDescription = document.getElementById('ticketDescription');
    if (ticketDescription) {
        if (ticketDescription.value && ticketDescription.value.length > 500) {
            setError(ticketDescription, 'ticketDescriptionError');
        }
    }

    // 5. 價格欄位驗證 (共6個)
    const priceFields = [
        { id: 'adultOriginalPrice', errId: 'adultOriginalPriceError' },
        { id: 'adultPrice', errId: 'adultPriceError' },
        { id: 'childOriginalPrice', errId: 'childOriginalPriceError' },
        { id: 'childPrice', errId: 'childPriceError' },
        { id: 'concessionOriginalPrice', errId: 'concessionOriginalPriceError' },
        { id: 'concessionPrice', errId: 'concessionPriceError' }
    ];

    priceFields.forEach(field => {
        const inputEl = document.getElementById(field.id);
        if (inputEl) {
            const val = inputEl.value;
            if (val === '' || isNaN(val) || parseInt(val) < 0) {
                setError(inputEl, field.errId);
                const setupCard = inputEl.closest('.price-setup-card');
                if (setupCard) {
                    setupCard.classList.add('border-danger');
                }
            }
        }
    });

    if (hasError) {
        e.preventDefault();
        if (firstErrorElement) {
            firstErrorElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return;
    }

    // 若通過驗證，則執行原有的圖片打包 logic
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

// 清除所有前端與後端錯誤狀態的事件監聽
document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById('ticketForm');
    if (!form) return;

    form.querySelectorAll('input[name], select[name], textarea[name]').forEach(function (el) {
        function clearErrors() {
            // 清除後端錯誤
            const serverErrors = document.querySelectorAll('.server-error-' + el.name);
            serverErrors.forEach(msg => msg.style.display = 'none');
            
            // 清除前端錯誤
            el.classList.remove('is-invalid');
            const clientErrorEl = document.getElementById(el.id + 'Error');
            if (clientErrorEl) {
                clientErrorEl.style.display = 'none';
            }

            // 特殊處理：門票分類
            if (el.name === 'ticketCategories') {
                const card = el.closest('.card');
                if (card) {
                    card.classList.remove('border-danger');
                }
                const catErr = document.getElementById('ticketCategoriesError');
                if (catErr) {
                    catErr.style.display = 'none';
                }
                const serverCatErr = document.querySelector('.server-error-ticketCategories');
                if (serverCatErr) {
                    serverCatErr.style.display = 'none';
                }
            }

            // 特殊處理：價格 card border
            if (el.id && el.id.includes('Price')) {
                const setupCard = el.closest('.price-setup-card');
                if (setupCard) {
                    // 只有當該卡片下的所有 input 都沒有 is-invalid 時，才移除 border-danger
                    const invalidInputs = setupCard.querySelectorAll('.is-invalid');
                    if (invalidInputs.length === 0) {
                        setupCard.classList.remove('border-danger');
                    }
                }
            }
        }

        el.addEventListener('input', clearErrors);
        el.addEventListener('change', clearErrors);
    });
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
