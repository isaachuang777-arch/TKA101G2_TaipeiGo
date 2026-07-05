/* ========================= 購物車資料 ========================= */
let cartData = [];

/* ========================= 頁面初始化 ========================= */
document.addEventListener("DOMContentLoaded", async function () {
    await checkExpiredProduct();   /***先檢查過期商品***/ 
    await loadCart();              /**再載入購物車***/ 
    document.getElementById("clearCartBtn").addEventListener("click", clearCart);
    document.getElementById("checkoutBtn").addEventListener("click", checkStockAndCheckout);
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
					<div class="product-date">  使用日期：${item.expiryDate.substring(0, 10)}</div>
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
	    const newQuantity = item.quantity + 1;
	    const cart = {
	        productId: item.productId,
	        productType: item.productType,
	        expiryDate: item.expiryDate,
	        spec: item.spec,
	        productQuantity: newQuantity
	    };

	    /* 首先!! 先確認庫存!!! */
	    const checkResponse = await fetch("/frontend/cart/checkUpdateStock", {
	        method: "POST",
	        headers: {
	            "Content-Type": "application/json"
	        },
	        body: JSON.stringify(cart)
	    });
	    if (!checkResponse.ok) {
	        const message = await checkResponse.text();
	        alert(message);
	        return;
	    }

	    /*  庫存OK才更新Redis !!!! */
	    const updateResponse = await fetch("/frontend/cart/updateCart", {
	        method: "POST",
	        headers: {
	            "Content-Type": "application/json"
	        },
	        body: JSON.stringify(cart)
	    });

	    if (updateResponse.ok) {
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
			const message = await response.text();
						   alert(message);
						   /*** 重新載入，恢復正確數量*/
						   await loadCart();        }
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
			const message = await response.text();
			   alert(message);
			   /*** 重新載入，恢復正確數量*/
			   await loadCart();        }
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

	/* ========================= 前往結帳前購物車自己再確認是否庫存足夠 ========================= */
	async function checkStockAndCheckout() {
	    if (cartData.length === 0) {
	        alert("購物車沒有商品");
	        return;
	    }
	    try {
	        const response = await fetch("/frontend/cart/checkBeforeCheckout", {
	            method: "POST"
	        });
	        if (response.ok) {
	            window.location.href = "/frontend/checkout";
	        } else {
	            const message = await response.text();
	            alert(message);
	        }
	    } catch (error) {
	        console.error(error);
	        alert("系統忙碌中，請稍後再試");
	    }
	}
	
/* ========================= 檢查並刪除過期商品 ========================= */
async function checkExpiredProduct() {
    try {
        /**先確認是否有過期商品**/ 
        const response = await fetch("/frontend/cart/checkExpired");
        if (!response.ok) {
            return;
        }
        const hasExpired = await response.json();
        if (hasExpired) {
            alert("購物車內有已過期商品，按下確定後將自動刪除。");
            /****呼叫刪除 API，不需要解析回傳值*/ 
            await fetch("/frontend/cart/removeExpired", {
                method: "POST"
            });
			/**重新導入購物車頁，Header購物車數量也會重新整理**/ 
			window.location.href = "/frontend/cart/shoppingCart";
			return;
        }
    } catch (error) {
        console.error(error);
        alert("系統忙碌中，請稍後再試");
    }
}