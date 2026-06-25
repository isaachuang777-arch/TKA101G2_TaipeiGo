
//fetch("/frontend/cart/queryCart")

/* ========================= 假資料========================= */
let cartData = [
    {
        productId: 1,
        productName: "九份老街一日遊",
        productPrice: 1500,
        productQuantity: 2,
        expiryDate: "2026-07-05",
        imageUrl: "https://picsum.photos/400/250?random=1"
    },
    {
        productId: 2,
        productName: "台北101觀景台門票",
        productPrice: 600,
        productQuantity: 1,
        expiryDate: "2026-07-10",
        imageUrl: "https://picsum.photos/400/250?random=2"
    }
];


/* ========================= 頁面初始化========================= */
document.addEventListener("DOMContentLoaded", function () {
    renderCart();
    document.getElementById("clearCartBtn").addEventListener("click", clearCart);

});


/* ========================= 渲染購物車========================= */
function renderCart() {
    const container =  document.getElementById("cartContainer");
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
        const subtotal =
            item.productPrice *
            item.productQuantity;
        container.innerHTML += `

            <div class="cart-item">
                <div class="cart-content">
                    <img  src="${item.imageUrl}" class="product-image">
                    <div class="product-info">
                        <div class="product-name"> ${item.productName}  </div>
                        <div class="product-date">  使用日期：  ${item.expiryDate}  </div>
                        <div class="product-price"> NT$ ${item.productPrice.toLocaleString()}  </div>
                    </div>
                </div>

                <div class="cart-footer">
                    <div class="quantity-area">
                        <button class="quantity-btn"  onclick="minusQuantity(${index})"> -  </button>
                        <span class="quantity-value"> ${item.productQuantity} </span>
                        <button class="quantity-btn"  onclick="plusQuantity(${index})">  + </button>
                        <button class="delete-btn"  onclick="removeItem(${index})"> 刪除 </button>
                    </div>
					
                    <div class="subtotal">
                        NT$ ${subtotal.toLocaleString()}
                    </div>
                </div>
            </div>
        `;
    });

    updateSummary();
}


/* ========================= 增加數量========================= */
function plusQuantity(index) {
    cartData[index].productQuantity++;
    renderCart();
    console.log("呼叫 updateCart API");
}


/* ========================減少數量========================= */
function minusQuantity(index) {
    if (cartData[index].productQuantity <= 1) {
        return;
    }
    cartData[index].productQuantity--;
    renderCart();
    console.log("呼叫 updateCart API");
}


/* ========================= 刪除單一商品========================= */

function removeItem(index) {
    const confirmDelete =confirm("確定要刪除此商品嗎？");
    if (!confirmDelete) {
        return;
    }

    cartData.splice(index, 1);
    renderCart();
    console.log("呼叫 removeCartProduct API");
}


/* =========================  清空購物車========================= */
function clearCart() {
    const confirmDelete =
        confirm("確定要清空購物車嗎？");
    if (!confirmDelete) {
        return;
    }

    cartData = [];
    renderCart();
    console.log("呼叫 clearCart API");
}


/* =========================
   更新摘要
========================= */
function updateSummary() {
    let totalCount = 0;
    let totalAmount = 0;

    cartData.forEach(item => {
        totalCount += item.productQuantity;
        totalAmount +=item.productPrice *item.productQuantity;
    });
    document.getElementById("totalCount") .textContent = totalCount;
    document.getElementById("totalAmount").textContent ="NT$ " + totalAmount.toLocaleString();
}


/* ========================= 前往結帳========================= */
document.addEventListener("DOMContentLoaded", function () {
    const checkoutBtn =document.getElementById("checkoutBtn");
    checkoutBtn.addEventListener("click", function () {
        if (cartData.length === 0) {
            alert("購物車沒有商品");
            return;
        }
        console.log("呼叫 checkout API");

        // location.href="/frontend/order/checkout";
    });
});





