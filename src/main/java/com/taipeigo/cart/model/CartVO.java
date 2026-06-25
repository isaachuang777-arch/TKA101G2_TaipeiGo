package com.taipeigo.cart.model;

import java.time.LocalDateTime;

public class CartVO {
		private Integer custId;
		private Integer productId;
		private Integer productQuantity;
		private LocalDateTime  expiryDate;
		private LocalDateTime createdAt;
		
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

		
		
		
		
		
		
}
