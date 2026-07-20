
# TaipeiGo (TKA101G2)
TaipeiGo 是一個基於 Spring Boot 開發的線上票券與活動預訂平台。專為探索城市活動、購買票券而設計，提供了完整的前台顧客瀏覽與購物體驗，以及嚴謹的後台管理系統。

## 專案結構簡介 (Project Structure)
專案主要模組劃分如下：
* `com.taipeigo.activity`: 活動模組
* `com.taipeigo.admin`: 後台管理員功能(包括管理員更改個人密碼以及 管理員管理中心) - (後台: 魏美雪)
* `com.taipeigo.auth`: 前台登入、註冊與帳號驗證 - (前台：李俊霖)
* `com.taipeigo.auth.controller.BackendAuthController`: 後台登入頁面與登入狀態導向 - (後台：魏美雪)
* `com.taipeigo.backend`: 後台通用邏輯或頁面
* `com.taipeigo.backend.controller`: 後台首頁（Dashboard） - (後台：李俊霖)
* `com.taipeigo.backend.security`: 身分驗證與安全攔截 (Spring Security) - (後台:魏美雪)
* `com.taipeigo.cart`: 購物車邏輯
* `com.taipeigo.checkout`: 結帳邏輯
* `com.taipeigo.common`: 共用工具或常數定義
* `com.taipeigo.config`: 系統配置與設定
* `com.taipeigo.cs`: 客服系統模組  - (前後台:魏美雪)
* `com.taipeigo.customer`: 前台會員功能與後台會員管理 - (前後台：李俊霖)
* `com.taipeigo.faq`: 常見問題模組  - (前台:魏美雪) (後台:李俊霖)
* `com.taipeigo.favorite`: 我的最愛功能 - (前台：李俊霖)
* `com.taipeigo.frontend.filter`: 前台登入攔截與頁面導向 - (前台：李俊霖)
* `com.taipeigo.myticket`: 個人票券夾管理 - (前台：李俊霖)
* `com.taipeigo.order`: 訂單管理
* `com.taipeigo.orders`: 歷史訂單管理
* `com.taipeigo.product`: 產品模組
* `com.taipeigo.ticket`: 票券模組
* `com.taipeigo.ticketcategory`: 票券類別模組

### 前台系統 (Frontend - 會員端)
* **會員功能**：顧客註冊、登入、個人資料與密碼修改、信箱驗證。
* **門票瀏覽**：各種票券展示、分類檢索。
* **一日活動瀏覽** ：各種一日活動展示、分類檢索。
* **購物車系統**：結合 Redis 進行購物車資料快取，提供流暢的選購與暫存體驗。
* **結帳與訂單**：完整的結帳流程、購買後的歷史訂單查詢。
* **票券夾與收藏**：專屬的「我的票券」集中管理、喜愛活動的「我的收藏」功能。
* **客戶服務**：新增客服查詢，查閱未結案和已結案列表。
* **常見問題 (FAQ)** 顯示常見問題。

### 後台系統 (Backend - 管理員端)
* **安全認證**：**導入 Spring Security** 進行後台登入防護與存取權限控管。
* **管理員管理中心**：檢視與管理所有管理員狀態以及更改密碼。　
* **會員管理**：檢視與管理所有註冊會員資料與狀態。
* **商品與票券管理**：票券類別設定、活動資訊更新、票券以及一日活動的上架與下架維護。
* **訂單管理**：處理顧客訂單、查詢與狀態追蹤。
* **內容管理**：維護前台常見問題 (FAQ) 的新增、修改與刪除。
  
##  技術棧 (Tech Stack)
* **Backend**: Java 17, Spring Boot, Spring Data JPA, Hibernate, Lombok
* **Security**: Spring Security (專門應用於後台管理系統的安全防護)
* **Frontend**: Thymeleaf, Bootstrap, JSTL, HTML/CSS/JS
* **Database**: MySQL 8.x, Redis (用於前台購物車、Email 驗證與重設密碼 Token)
* **Others**: JavaMail (Gmail SMTP 寄信服務), JSON/Gson, Maven



  
## 👥 開發團隊 (Team)
* TKA101 第二組
