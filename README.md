
# TaipeiGo (TKA101G2)
TaipeiGo 是一個基於 Spring Boot 開發的線上票券與活動預訂平台。專為探索城市活動、購買票券而設計，提供了完整的前台顧客瀏覽與購物體驗，以及嚴謹的後台管理系統。

## 專案結構簡介 (Project Structure)
專案主要模組劃分如下：
* `com.taipeigo.product: 系統商品總表核心模組 (包含 AOP 資料同步機制、Facade 對接窗口、SearchResultDTO / CartItemDTO 等跨模組共用封裝) - (前後台: 黃裕舜)
* `com.taipeigo.activity: 活動與分類管理模組 (前後台 CRUD、全站搜尋引擎 Stream API 實作) - (前後台: 黃裕舜)
* `com.taipeigo.activity (Category): 一日活動分類與標籤模組 (與活動模組高度整合，包含完整的分類管理邏輯) - (前後台: 黃裕舜)
* `com.taipeigo.admin`: 後台管理員功能(包括管理員更改個人密碼以及 管理員管理中心) - (後台: 魏美雪)
* `com.taipeigo.auth`: 前台登入、註冊與帳號驗證 - (前台：李俊霖)
* `com.taipeigo.auth.controller.BackendAuthController`: 後台登入頁面與登入狀態導向 - (後台：魏美雪)
* `com.taipeigo.backend`: 後台通用邏輯或頁面
* `com.taipeigo.backend.controller`: 後台首頁（Dashboard） - (後台：李俊霖)
* `com.taipeigo.backend.security`: 身分驗證與安全攔截 (Spring Security) - (後台:魏美雪)
* `com.taipeigo.cart`: 購物車功能(CRUD, 確認庫存API) - (前台:黃依甯)
* `com.taipeigo.checkout`: 結帳功能(付款後新增訂單API, 結帳前確認庫存API, 刪除無庫存API) - (前台:黃依甯)
* `com.taipeigo.common`: 共用工具或常數定義
* `com.taipeigo.config`: 系統配置與設定
* `com.taipeigo.cs`: 客服系統模組  - (前後台:魏美雪)
* `com.taipeigo.customer`: 前台會員功能與後台會員管理 - (前後台：李俊霖)
* `com.taipeigo.faq`: 常見問題模組  - (前台:魏美雪) (後台:李俊霖)
* `com.taipeigo.favorite`: 我的最愛功能 - (前台：李俊霖)
* `com.taipeigo.frontend.filter`: 前台登入攔截與頁面導向 - (前台：李俊霖)
* `com.taipeigo.myticket`: 個人票券夾管理 - (前台：李俊霖)
* `com.taipeigo.orders`: 訂單管理功能(前台:會員訂單及訂單明細查詢API, 後台:訂單及訂單明細查詢API, 訂單編號查詢API, 會員編號查詢API, 更新訂單狀態API) - (前台/後台:黃依甯)
* `com.taipeigo.order_detail`: 訂單明細功能(訂單明細查詢)- (後台:黃依甯)
* `com.taipeigo.ticket`: 票券模組 - (前後台 :陳俞瑾)
* `com.taipeigo.ticketcategory`: 票券類別模組 - (前後台 :陳俞瑾)

### 前台系統 (Frontend - 會員端)
* **會員功能**：顧客註冊、登入、個人資料與密碼修改、信箱驗證。
* **全站商品搜尋引擎**：支援模糊搜尋、多條件標籤篩選與價格排序，快速檢索全站門票與活動。
* **門票與活動瀏覽**：各種票券與一日活動展示、分類檢索。
* **購物車系統**：結合 Redis 進行購物車資料快取，提供流暢的選購與暫存體驗。
* **結帳與訂單**：完整的結帳流程、購買後的歷史訂單查詢。
* **票券夾與收藏**：專屬的「我的票券」集中管理、喜愛活動的「我的收藏」功能。
* **客戶服務**：新增客服查詢，查閱未結案和已結案列表。
* **常見問題 (FAQ)**：顯示常見問題。

### 後台系統 (Backend - 管理員端)
* **安全認證**：導入 Spring Security 進行後台登入防護與存取權限控管。
* **商品總表即時監控**：提供商品總表看板，即時動態展示跨模組新增的商品資訊。
* **活動與票券管理**：提供一日活動與門票的獨立 CRUD 介面，支援商品上架、下架與分類標籤維護。
* **管理員中心**：檢視與管理所有管理員狀態以及更改密碼。
* **會員管理**：檢視與管理所有註冊會員資料與狀態。
* **訂單與內容管理**：處理顧客訂單狀態追蹤；維護前台常見問題 (FAQ) 內容。
  
### 技術棧 (Tech Stack)
* **Backend**: Java 17, Spring Boot, Spring MVC, Spring Data JPA, Hibernate, JDBC, Spring AOP, Lombok
* **Security**: Spring Security (專門應用於後台管理系統的安全防護)
* **Frontend**: Thymeleaf, Bootstrap, JSTL, HTML/CSS/JS, AJAX
* **Database**: MySQL 8.x, Redis (用於前台購物車、Email 驗證與重設密碼 Token)
* **Others**: RESTful API, JavaMail (Gmail SMTP 寄信), JSON/Gson, Maven


## 👥 開發團隊 (Team)
* TKA101 第二組
* 黃裕舜 (組長)：全站搜尋引擎、系統底層架構整合、一日活動與分類管理、商品總表模組
* 李俊霖：會員模組、前端登入攔截、我的最愛
* 魏美雪：後台管理、Spring Security、客服與 FAQ 模組
* 黃依甯：購物車模組、結帳與訂單系統
* 陳俞瑾：票券模組與票券分類
