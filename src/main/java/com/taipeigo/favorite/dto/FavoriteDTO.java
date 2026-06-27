package com.taipeigo.favorite.dto;

import java.io.Serializable;

public class FavoriteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer favoriteNo;     // 我的最愛編號
    private Integer productId;      // 商品編號
    private Integer itemId;         // ActivityId 或 TicketId
    private String productName;     // 商品名稱
    private String productType;     // ACTIVITY 或 TICKET
    private String imageUrl;        // 商品圖片

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}