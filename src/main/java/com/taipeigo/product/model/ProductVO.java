package com.taipeigo.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "PRODUCTS")
public class ProductVO implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Integer productId;

    // 因為商品可以是 Activity 也可以是 Ticket，所以這兩個 FK 必須允許為 null
    @Column(name = "ACTIVITY_ID", nullable = true)
    private Integer activityId;

    @Column(name = "TICKET_ID", nullable = true)
    private Integer ticketId;

    @NotBlank(message = "商品名稱必填")
    @Size(max = 50, message = "商品名稱不能超過50個字")
    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    // 0=下架, 1=上架
    @NotNull(message = "商品上架狀態不能為空")
    @Column(name = "STATUS", nullable = false)
    private Integer status;



    public ProductVO() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }



}
