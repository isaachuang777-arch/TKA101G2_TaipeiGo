package com.taipeigo.favorite.dto;

import java.io.Serializable;

public class FavoriteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer favoriteNo;     // 我的最愛編號
    private Integer productId;      // PRODUCTS 的商品編號
    private Integer itemId;         // 活動ID或門票ID
    private String productName;     // 商品名稱
    private String productType;     // ACTIVITY 或 TICKET
    private Integer price;          // 顯示價格
    private String imageUrl;        // 商品圖片
    private String detailUrl;       // 查看詳情連結
    private Byte status;            // 上架狀態

    public FavoriteDTO() {
    }

    public Integer getFavoriteNo() {
        return favoriteNo;
    }

    public void setFavoriteNo(Integer favoriteNo) {
        this.favoriteNo = favoriteNo;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}