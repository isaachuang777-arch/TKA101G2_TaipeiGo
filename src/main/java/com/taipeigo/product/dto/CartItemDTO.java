package com.taipeigo.product.dto;

import java.io.Serializable;

public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId; // 來自 PRODUCTS 表的 ID
    private String productName; // 商品名稱
    private Integer price; // 最終結帳價格 (由 Service 動態計算後塞入)
    private Integer quantity; // 購買數量
    private String productType; // 標記 "ACTIVITY" 或 "TICKET"
    private String imageUrl; // 購物車上的顯示縮圖

    public CartItemDTO() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
