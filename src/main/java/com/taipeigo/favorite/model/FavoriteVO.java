package com.taipeigo.favorite.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "FAVORITE",
    // 同一位會員不能重複收藏同一個商品
    // 例如：CUST_ID 1 + PRODUCT_ID 1、CUST_ID 1 + PRODUCT_ID 1 > 不行
    //      CUST_ID 1 + PRODUCT_ID 1、CUST_ID 1 + PRODUCT_ID 2 > 可以
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_CUST_PRODUCT",
            columnNames = {"CUST_ID", "PRODUCT_ID"}
        )
    }
)
public class FavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FAVORITE_NO")
    private Integer favoriteNo;

    @Column(name = "CUST_ID", nullable = false)
    private Integer custId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Integer productId;

    public FavoriteVO() {
    }

    public Integer getFavoriteNo() {
        return favoriteNo;
    }

    public void setFavoriteNo(Integer favoriteNo) {
        this.favoriteNo = favoriteNo;
    }

    public Integer getCustId() {
        return custId;
    }

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}