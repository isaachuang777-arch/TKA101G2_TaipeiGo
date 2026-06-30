/* ========================= 初始化 ========================= */
document.addEventListener("DOMContentLoaded", function () {

    const cardName = document.getElementById("cardName");
    const cardNumber = document.getElementById("cardNumber");
    const expiry = document.getElementById("expiry");
    const cvv = document.getElementById("cvv");
    const payBtn = document.getElementById("payBtn");

    const cardNameError = document.getElementById("cardNameError");
    const cardNumberError = document.getElementById("cardNumberError");
    const expiryError = document.getElementById("expiryError");
    const cvvError = document.getElementById("cvvError");

    let inputType = "";

    /* ========================= 持卡人姓名 ========================= */
    cardName.addEventListener("input", function () {

        let value = this.value;

        if (value === "") {
            inputType = "";
            cardNameError.innerText = "";
            checkForm();
            return;
        }

        const lastChar = value.charAt(value.length - 1);

        if (/[\u4e00-\u9fa5]/.test(lastChar)) {

            if (inputType === "") inputType = "chinese";

            if (inputType !== "chinese") {
                this.value = value.slice(0, -1);
                cardNameError.innerText = "不可中英文混合";
                checkForm();
                return;
            }

        } else if (/[A-Za-z ]/.test(lastChar)) {

            if (inputType === "") inputType = "english";

            if (inputType !== "english") {
                this.value = value.slice(0, -1);
                cardNameError.innerText = "不可中英文混合";
                checkForm();
                return;
            }

        } else {

            this.value = value.slice(0, -1);
            cardNameError.innerText = "只能輸入中文或英文";
            checkForm();
            return;

        }

        cardNameError.innerText = "";
        checkForm();

    });


    /* ========================= 信用卡號 ========================= */
    cardNumber.addEventListener("input", function () {

        let value = this.value.replace(/\D/g, "");

        value = value.substring(0, 16);

        value = value.replace(/(.{4})/g, "$1 ").trim();
        this.value = value;
        if (value.replace(/\s/g, "").length === 16) {
            cardNumberError.innerText = "";
        } else {
            cardNumberError.innerText = "信用卡號需16碼";
        }
        checkForm();
    });


/* ========================= 有效期限 ========================= */
    expiry.addEventListener("input", function () {
        let value = this.value.replace(/\D/g, "");
        if (value.length > 4) {
            value = value.substring(0, 4);
        }
        if (value.length >= 3) {
            value = value.substring(0, 2) + "/" + value.substring(2);
        }
        this.value = value;
        validateExpiry();
    });


    function validateExpiry() {
        const value = expiry.value;
        if (value === "") {
            expiryError.innerText = "";
            checkForm();
            return;
        }
        if (!/^\d{2}\/\d{2}$/.test(value)) {
            expiryError.innerText = "格式需為 MM/YY";
            checkForm();
            return;
        }
        const month = parseInt(value.substring(0, 2));
        const year = parseInt("20" + value.substring(3));
        if (month < 1 || month > 12) {
            expiryError.innerText = "月份錯誤";
            checkForm();
            return;
        }
        const today = new Date();
        const currentYear = today.getFullYear();
        const currentMonth = today.getMonth() + 1;

        if (
            year < currentYear ||
            (year === currentYear && month < currentMonth)
        ) {
            expiryError.innerText = "信用卡已過期";
        } else {
            expiryError.innerText = "";
        }

        checkForm();

    }


 /* ========================= CVV ========================= */
    cvv.addEventListener("input", function () {
        this.value = this.value.replace(/\D/g, "").substring(0, 3);

        if (this.value.length === 3) {
            cvvError.innerText = "";
        } else {
            cvvError.innerText = "CVV需3碼";
        }
        checkForm();
    });


 /* ========================= 檢查表單 ========================= */
    function checkForm() {
        const nameOK =
            cardName.value.trim() !== "" &&
            cardNameError.innerText === "";
        const cardOK = cardNumber.value.replace(/\s/g, "").length === 16;
        const expiryOK =expiryError.innerText === "" && /^\d{2}\/\d{2}$/.test(expiry.value);
        const cvvOK = /^\d{3}$/.test(cvv.value);
        payBtn.disabled = !(nameOK && cardOK && expiryOK && cvvOK);
    }


 /* ========================= 綁定付款 ========================= */
    payBtn.addEventListener("click", pay);

    checkForm();

});


/* ========================= 付款 ========================= */
async function pay() {
    const cardName = document.getElementById("cardName").value.trim();
    const cardNumber = document.getElementById("cardNumber").value.replace(/\s/g, "");
    const expiry = document.getElementById("expiry").value;
    const cvv = document.getElementById("cvv").value;
    if (cardName === "") {
        alert("請輸入持卡人姓名");
        return;
    }
    if (cardNumber.length !== 16) {
        alert("信用卡號格式錯誤");
        return;
    }
    if (!/^\d{2}\/\d{2}$/.test(expiry)) {
        alert("有效期限格式錯誤");
        return;
    }
    if (!/^\d{3}$/.test(cvv)) {
        alert("CVV格式錯誤");
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
            throw new Error("付款失敗");
        }
        alert("付款成功！");
        location.href = "/frontend/customer/orders";
    } catch (error) {
        console.error(error);
        alert("付款失敗，請稍後再試");

    }

}