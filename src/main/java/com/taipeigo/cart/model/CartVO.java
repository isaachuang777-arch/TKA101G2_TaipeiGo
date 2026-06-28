package com.taipeigo.cart.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CartVO implements Serializable{
    private static final long serialVersionUID = 1L;

		private Integer custId;
		private Integer productId;
		private Integer productQuantity;
		private LocalDateTime  expiryDate;
		private LocalDateTime createdAt;
		/*標記 ACTIVITY 或 TICKET*/
		private String productType;
		/*標記票種，例如成人票、兒童票，若為一般活動則可為空白或預設值*/
		private String spec;
		/*用來記住前端使用者的路徑*/
		private String currentUrl;

		
		public CartVO() {
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


		public Integer getProductQuantity() {
			return productQuantity;
		}


		public void setProductQuantity(Integer productQuantity) {
			this.productQuantity = productQuantity;
		}


		public LocalDateTime getExpiryDate() {
			return expiryDate;
		}


		public void setExpiryDate(LocalDateTime expiryDate) {
			this.expiryDate = expiryDate;
		}


		public LocalDateTime getCreatedAt() {
			return createdAt;
		}


		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}


		public String getProductType() {
			return productType;
		}


		public void setProductType(String productType) {
			this.productType = productType;
		}


		public String getSpec() {
			return spec;
		}


		public void setSpec(String spec) {
			this.spec = spec;
		}


		public String getCurrentUrl() {
			return currentUrl;
		}


		public void setCurrentUrl(String currentUrl) {
			this.currentUrl = currentUrl;
		}
		
		
		
		
}
