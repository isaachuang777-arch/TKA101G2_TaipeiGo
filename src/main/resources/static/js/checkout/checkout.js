/* ========================= 初始化 ========================= */
document.addEventListener("DOMContentLoaded", function () {
	document.getElementById("payBtn").addEventListener("click", pay);
    document.getElementById("cardNumber").addEventListener("input", formatCardNumber);
    document.getElementById("expireDate").addEventListener("input", formatExpireDate);
});


/* ========================= 信用卡格式 ========================= */
function formatCardNumber(e) {
    let value = e.target.value.replace(/\D/g, "");
    value = value.substring(0, 16);
    value = value.replace(/(.{4})/g, "$1 ").trim();
    e.target.value = value;
}


/* ========================= MM/YY ========================= */
function formatExpireDate(e) {
    let value = e.target.value.replace(/\D/g, "");
    if (value.length > 4) {
        value = value.substring(0, 4);
    }
    if (value.length >= 3) {
        value = value.substring(0, 2) + "/" + value.substring(2);
    }
    e.target.value = value;
}


/* ========================= 付款 ========================= */
async function pay() {
    const cardName = document.getElementById("cardName").value.trim();
    const cardNumber = document.getElementById("cardNumber").value.replace(/\s/g, "");
    const expireDate = document.getElementById("expireDate").value;
    const cvv = document.getElementById("cvv").value;
    if (cardName === "") {
        alert("請輸入持卡人姓名");
        return;
    }
    if (cardNumber.length !== 16) {
        alert("信用卡號格式錯誤");
        return;
    }
    if (!/^\d{2}\/\d{2}$/.test(expireDate)) {
        alert("有效期限格式錯誤");
        return;
    }
    if (!/^\d{3}$/.test(cvv)) {
        alert("CVV 格式錯誤");
        return;
    }
    if (!confirm("確認付款？")) {
        return;
    }
    try {
        const response = await fetch("/frontend/checkout/pay", {
            method: "POST"
        });
        if (!response.ok) {
            throw new Error();
        }
        alert("付款成功！");
        location.href = "/frontend/customer/orders";
    } catch (error) {
        console.error(error);
        alert("付款失敗，請稍後再試");
    }

}