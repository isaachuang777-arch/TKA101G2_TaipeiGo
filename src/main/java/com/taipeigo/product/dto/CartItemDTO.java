package com.taipeigo.product.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;         // 來自 PRODUCTS 表的 ID
    private String productName;        // 商品名稱
    private Integer price;             // 最終結帳價格
    private Integer quantity;          // 購買數量
    private String productType;        // 標記 "ACTIVITY" 或 "TICKET"
    private String imageUrl;           // 購物車上的顯示縮圖
    
    private Integer subtotal;           // 小計 (單價 * 數量)
    private LocalDateTime expiryDate;   // 票券到期日 (如果是活動，這個欄位就
    private String spec;                // 記錄票種用的 - 成人票兒童票之類的

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

    public Integer getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    
    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

}
