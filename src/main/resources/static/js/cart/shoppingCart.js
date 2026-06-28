/* ========================= 購物車資料 ========================= */
let cartData = [];

/* ========================= 頁面初始化 ========================= */
document.addEventListener("DOMContentLoaded", function () {
    loadCart();
    document.getElementById("clearCartBtn")
        .addEventListener("click", clearCart);

    document.getElementById("checkoutBtn")
        .addEventListener("click", checkout);
});

/* ========================= 讀取購物車 ========================= */
async function loadCart() {
    try {
        const response = await fetch("/frontend/cart/queryCartDetail");
        if (!response.ok) {
            throw new Error("讀取購物車失敗");
        }
        cartData = await response.json();
        renderCart();
    } catch (error) {
        console.error(error);
        alert("購物車資料載入失敗");
    }
}

/* ========================= 渲染 ========================= */
function renderCart() {
    const container = document.getElementById("cartContainer");
    container.innerHTML = "";
    if (cartData.length === 0) {
        container.innerHTML = `
            <div class="empty-cart">
                <h2>購物車目前沒有商品</h2>
                <p>快去挑選喜歡的旅遊商品吧！</p>
            </div>
        `;
        updateSummary();
        return;
    }

    cartData.forEach((item, index) => {
        container.innerHTML += `
        <div class="cart-item">
            <div class="cart-content">
                <img src="${item.imageUrl}" class="product-image">
                <div class="product-info">
                    <div class="product-name">  ${item.productName} </div>
                    <div class="product-date">  使用日期：${item.expiryDate} </div>
                    <div class="product-price"> NT$ ${item.price.toLocaleString()} </div>
                </div>
            </div>
            <div class="cart-footer">
                <div class="quantity-area">
                    <button class="quantity-btn"  onclick="minusQuantity(${index})"> - </button>
                    <span class="quantity-value">  ${item.quantity} </span>
                    <button class="quantity-btn"  onclick="plusQuantity(${index})"> +  </button>
                    <button class="delete-btn" onclick="removeItem(${index})">  刪除 </button>
                </div>
                <div class="subtotal"> NT$ ${item.subtotal.toLocaleString()}</div>
            </div>

        </div>

        `;
    });
    updateSummary();
}

/* ========================= + ========================= */
async function plusQuantity(index) {
    const item = cartData[index];
    item.quantity++;

    const cart = {
        productId: item.productId,
        productType: item.productType,
        expiryDate: item.expiryDate,
        spec: item.spec,
        productQuantity: item.quantity
    };

    const response = await fetch("/frontend/cart/updateCart", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(cart)
    });

    if (response.ok) {
        await loadCart();
        if (typeof loadCartCount === "function") {
            loadCartCount();
        }
    } else {
        alert("更新失敗");
    }
}


/* ========================= - ========================= */
async function minusQuantity(index) {
    const item = cartData[index];

    if (item.quantity <= 1) {
        return;
    }

    item.quantity--;
    const cart = {
        productId: item.productId,
        productType: item.productType,
        expiryDate: item.expiryDate,
        spec: item.spec,
        productQuantity: item.quantity
    };

    const response = await fetch("/frontend/cart/updateCart", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(cart)
    });

    if (response.ok) {
        await loadCart();
        if (typeof loadCartCount === "function") {
            loadCartCount();
        }
    } else {
        alert("更新失敗");
    }
}

/* ========================= 刪除 ========================= */
async function removeItem(index) {
    if (!confirm("確定要刪除此商品嗎？")) {
        return;
    }

    const item = cartData[index];
    const cart = {
        productId: item.productId,
        productType: item.productType,
        expiryDate: item.expiryDate,
        spec: item.spec
    };

    const response = await fetch("/frontend/cart/removeCartProduct", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(cart)
    });

    if (response.ok) {
        await loadCart();
        if (typeof loadCartCount === "function") {
            loadCartCount();
        }
    } else {
        alert("刪除失敗");
    }
}

/* ========================= 清空購物車 ========================= */
async function clearCart() {
    if (!confirm("確定要清空購物車嗎？")) {
        return;
    }
    const response = await fetch("/frontend/cart/clearCart", {
        method: "DELETE"
    });
    if (response.ok) {
        await loadCart();
        if (typeof loadCartCount === "function") {
            loadCartCount();
        }
        alert("購物車已清空");
    } else {
        alert("清空失敗");
    }
}

/* ========================= 更新摘要 ========================= */
function updateSummary() {
    let totalCount = 0;
    let totalAmount = 0;
    cartData.forEach(item => {
        totalCount += item.quantity;
        totalAmount += item.subtotal;
    });
    document.getElementById("totalCount").textContent = totalCount;
    document.getElementById("totalAmount").textContent = "NT$ " + totalAmount.toLocaleString();

}

/* ========================= 前往結帳 ========================= */
async function checkout() {
    if (cartData.length === 0) {
        alert("購物車沒有商品");
        return;
    }

    if (!confirm("確認送出訂單？")) {
        return;
    }
    const response = await fetch("/frontend/checkout", {
        method: "POST"
    });

    if (response.ok) {
        alert("訂單建立成功");
        location.href = "/frontend/customer/orders";
    } else {
        alert("結帳失敗");
    }
}