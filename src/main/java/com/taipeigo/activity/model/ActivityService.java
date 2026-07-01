package com.taipeigo.activity.model;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepo;
    private final ActivityJDBCDAO activityJDBCDAO;
    private final ActivityCateRepository activityCateRepo;
    private final TicketService ticketService;

    @Autowired
    public ActivityService(ActivityRepository activityRepo, 
                           ActivityJDBCDAO activityJDBCDAO,
                           ActivityCateRepository activityCateRepo,
                           TicketService ticketService) {

        this.activityRepo = activityRepo;
        this.activityJDBCDAO = activityJDBCDAO;
        this.activityCateRepo = activityCateRepo;
        this.ticketService = ticketService;

    }


    // -----------------單一查詢---------------------

    public ActivityVO getActivityVOById(Integer activityId) {

        Optional<ActivityVO> box = activityRepo.findById(activityId);

        if (box.isEmpty()) {

            throw new RuntimeException("找不到該活動 " + activityId);
        }

        ActivityVO activity = box.get();

        return activity;
    }

    // -----------------前台萬用查詢-----------------

    public List<ActivityVO> getActivitiesByCompositeQuery(MultiValueMap<String, String> map) {

        return activityJDBCDAO.getSearch(map, true);
    }

    // -----------------後台萬用查詢-----------------

    public List<ActivityVO> getBackendActivitiesByCompositeQuery(MultiValueMap<String, String> map) {

        return activityJDBCDAO.getSearch(map, false);
    }

    public int getTotalPageByCompositeQuery(MultiValueMap<String, String> map){

        return activityJDBCDAO.getTotalPage(map);


    }

    // ----------------- 圖片路徑 -----------------

    @Value("${taipeigo.upload.base-dir}")
    private String uploadBaseDir;

    private static final String DEFALUT_IMG_URL = "/images/activity/default.png";

    private String getUploadDir() {

        if (uploadBaseDir == null || uploadBaseDir.isBlank()) {

            throw new IllegalStateException("圖片路徑遺失");
        }

        String base = uploadBaseDir.endsWith("/") ? uploadBaseDir : uploadBaseDir + "/";

        return base + "activity/";
    }

    // ----------------- 後臺新增 -----------------

    public void addActivity(ActivityVO activity, List<Integer> ticketIds, MultipartFile[] images, List<Integer> cateIds) {

        // 組合一日活動

        List<ActivityDetailVO> details = new ArrayList<>();

        for (int i = 0; i < ticketIds.size(); i++) {

            TicketVO ticketProxy = new TicketVO();

            ticketProxy.setTicketId(ticketIds.get(i));

            ActivityDetailVO detailVO = new ActivityDetailVO();

            detailVO.setTicket(ticketProxy);
            detailVO.setActivity(activity);
            detailVO.setSequence(i + 1);

            details.add(detailVO);
        }

        activity.setActivityDetails(details);

        // 處理活動類別標籤
        if (cateIds != null && !cateIds.isEmpty()) {
            List<ActivityCateInfoVO> cateInfoList = new ArrayList<>();
            for (Integer cateId : cateIds) {
                ActivityCateVO cateProxy = new ActivityCateVO();
                cateProxy.setActivityCateId(cateId);

                ActivityCateInfoVO infoVO = new ActivityCateInfoVO();
                infoVO.setActivity(activity);
                infoVO.setActivityCate(cateProxy);

                cateInfoList.add(infoVO);
            }
            activity.setActivityCateInfoVO(cateInfoList);
        }

        // 處理多張圖片上傳

        List<ActivityImageVO> imgList = new ArrayList<>();

        System.out.println("====== DEBUG: addActivity received images length: " + (images != null ? images.length : "null") + " ======");

        if (images != null && images.length > 0 && !images[0].isEmpty()) {

            for (MultipartFile file : images) {

                if (!file.isEmpty()) {

                    String fileUrl = saveUploadedFile(file);

                    ActivityImageVO imageVO = new ActivityImageVO();

                    imageVO.setActivity(activity);
                    imageVO.setActivityImageSrc(fileUrl);

                    imgList.add(imageVO);
                    System.out.println("====== DEBUG: Saved image: " + fileUrl + " ======");

                }
            }
        } else {

            ActivityImageVO defaultImg = new ActivityImageVO();

            defaultImg.setActivityImageSrc(DEFALUT_IMG_URL);
            defaultImg.setActivity(activity);
            imgList.add(defaultImg);
        }

        activity.setActivityImage(imgList);

        activityRepo.save(activity);
    }

    // ----------------- 圖檔儲存方法 -----------------

    private String saveUploadedFile(MultipartFile file) {

        try {

            File uploadDir = new File(getUploadDir());

            if (!uploadDir.exists())
                uploadDir.mkdirs();

            // 避免同名被覆蓋處理

            String originalName = file.getOriginalFilename();

            String extension = originalName.substring(originalName.lastIndexOf("."));

            String newFileName = java.util.UUID.randomUUID().toString() + extension;

            File dest = new File(getUploadDir() + newFileName);

            file.transferTo(dest);

            return "/images/activity/" + newFileName;

        } catch (IOException ie) {

            throw new RuntimeException("圖片上傳失敗" + ie.getMessage());
        }
    }

    // ----------------- 後臺修改 -----------------

    public void updateActivity(Integer activityId, ActivityVO updatedActivity,
            List<Integer> newTicketId, MultipartFile[] newImages, List<Integer> deleteImageIds, List<Integer> cateIds) {

        Optional<ActivityVO> box = activityRepo.findById(activityId);

        if (box.isEmpty()) {
            throw new RuntimeException("找不到該活動");
        }

        ActivityVO existActivityVO = box.get();

        existActivityVO.setActivityName(updatedActivity.getActivityName());
        existActivityVO.setActivityDesc(updatedActivity.getActivityDesc());
        existActivityVO.setExpiryDate(updatedActivity.getExpiryDate());
        existActivityVO.setDiscount(updatedActivity.getDiscount());
        existActivityVO.setActivityStatus(updatedActivity.getActivityStatus());

        List<ActivityDetailVO> oldDetailVOs = existActivityVO.getActivityDetails();

        Iterator<ActivityDetailVO> iterator = oldDetailVOs.iterator();

        // 先找原本就有的名單去跟前端塞進來的對，沒一樣就刪除，有一樣就改Sequence

        while (iterator.hasNext()) {

            ActivityDetailVO detail = iterator.next();

            Integer oldId = detail.getTicket().getTicketId();

            boolean isNewInList = false;

            for (int j = 0; j < newTicketId.size(); j++) {

                Integer newId = newTicketId.get(j);

                if (oldId.equals(newId)) {

                    isNewInList = true;

                    detail.setSequence(j + 1);

                    break;
                }

            }

            if (isNewInList == false) {

                iterator.remove();

            }

        }

        // 找出前端塞進來的新名單跟就名單對(跟上面的相反)，沒一樣就新增

        for (int i = 0; i < newTicketId.size(); i++) {

            Integer newId = newTicketId.get(i);

            boolean isOldinList = false;

            for (int j = 0; j < oldDetailVOs.size(); j++) {

                ActivityDetailVO detail = oldDetailVOs.get(j);

                Integer oldId = detail.getTicket().getTicketId();

                if (newId.equals(oldId)) {

                    isOldinList = true;

                    break;
                }
            }

            if (isOldinList == false) {

                TicketVO proxy = new TicketVO();

                proxy.setTicketId(newId);

                ActivityDetailVO newDetail = new ActivityDetailVO();

                newDetail.setTicket(proxy);
                newDetail.setSequence(i + 1);
                newDetail.setActivity(existActivityVO);

                oldDetailVOs.add(newDetail);

            }

        }

        // 修改圖片處理

        if (newImages != null && newImages.length > 0 && !newImages[0].isEmpty()) {


            for (MultipartFile file : newImages) {

                if (!file.isEmpty()) {

                    String fileUrl = saveUploadedFile(file);

                    ActivityImageVO imgVO = new ActivityImageVO();

                    imgVO.setActivity(existActivityVO);
                    imgVO.setActivityImageSrc(fileUrl);

                    existActivityVO.getActivityImage().add(imgVO);

                }
            }
        }


        // 刪除舊圖片

        if(deleteImageIds != null && !deleteImageIds.isEmpty()){
             // 這邊一定要用 Iterator 不然list會自動補位又用foreach的話會發生 ConcurrentModificationException 

             Iterator<ActivityImageVO> imgIter = existActivityVO.getActivityImage().iterator();
             
             while (imgIter.hasNext()) {

                ActivityImageVO img = imgIter.next();

                if(deleteImageIds.contains(img.getActivityImageId())){
                    img.setActivity(null); // 解除雙向關聯，確保 Hibernate 刪除
                    imgIter.remove();
                }
                
             }


        }

        // 修改活動類別標籤
        List<ActivityCateInfoVO> oldCateInfoList = existActivityVO.getActivityCateInfoVO();
        if (oldCateInfoList == null) {
            oldCateInfoList = new ArrayList<>();
            existActivityVO.setActivityCateInfoVO(oldCateInfoList);
        } else {
            // 清空舊的標籤 (因為有 orphanRemoval = true，Hibernate 會自動刪除對應的資料庫記錄)
            oldCateInfoList.clear();
        }

        if (cateIds != null && !cateIds.isEmpty()) {
            for (Integer cateId : cateIds) {
                ActivityCateVO cateProxy = new ActivityCateVO();
                cateProxy.setActivityCateId(cateId);

                ActivityCateInfoVO infoVO = new ActivityCateInfoVO();
                infoVO.setActivity(existActivityVO);
                infoVO.setActivityCate(cateProxy);

                oldCateInfoList.add(infoVO);
            }
        }

        activityRepo.save(existActivityVO);

    }

    // ----------------- 取得所有門票供表單選擇 -----------------
    public List<TicketVO> getAllTickets() {
        return ticketService.getAll();
    }
    

    // ----------------- 前台取得所有啟用中的分類 -----------------
    public List<ActivityCateVO> getAllActiveCategories() {

        return activityCateRepo.findAllActiveCategories();
    }

    // ----------------- 後台：一鍵切換上下架狀態 -----------------

    public void updateActivityStatus(Integer activityId, Integer newStatus){

        ActivityVO activity = activityRepo.findById(activityId).orElseThrow
                              (()-> new RuntimeException("找不到該活動 ID : " + activityId ));

        
        // 更新狀態

        activity.setActivityStatus(newStatus);

        // 存回資料庫

        activityRepo.save(activity);
    }


    // ----------------- 購物車顯示用功能 -----------------

    public CartItemDTO getActivityCartItem(Integer activityId, Integer quantity, String spec){

        ActivityVO activity = activityRepo.findById(activityId)
                            .orElseThrow(() -> new RuntimeException("找不到該活動 " + activityId));

        int totalPrice = 0;
        String typeName = "";


        // 1. 動態計算活動總價
        for (ActivityDetailVO detail : activity.getActivityDetails()) {

            if (detail.getTicket().getAvailableSerialCount() < 1) {

                throw new IllegalArgumentException("活動包含的某些門票已售完，無法加入購物車！");
            }

            switch (spec.toUpperCase()) {

                case "CHILD":
                    totalPrice += detail.getTicket().getChildPrice();
                    typeName = "兒童票";
                    break;

                case "CONCESSION":
                    totalPrice += detail.getTicket().getConcessionPrice();
                    typeName = "優待票";
                    break;

                case "ADULT":
                    totalPrice += detail.getTicket().getAdultPrice();
                    typeName = "成人票";
                    break;

                default:
                    throw new IllegalArgumentException("未知的票種: " + spec);
            }
        }
        // 2. 扣除活動專屬折扣，並設定防呆最低手續費 30 元
        int finalPrice = totalPrice - activity.getDiscount();

        if (finalPrice < 30) {
            finalPrice = 30; 
        }
        
        CartItemDTO cartItem = new CartItemDTO();
        cartItem.setProductId(activityId);
        cartItem.setProductType("ACTIVITY");
        cartItem.setSpec(spec);          // 記錄票種
        cartItem.setQuantity(quantity);  // 記錄數量
        
        cartItem.setPrice(finalPrice);
        cartItem.setProductName(activity.getActivityName() + " (" + typeName + ")");
        cartItem.setSubtotal(finalPrice * quantity);
        
        // 抓活動圖片
        if (activity.getActivityImage() != null && !activity.getActivityImage().isEmpty()) {
            cartItem.setImageUrl(activity.getActivityImage().get(0).getActivityImageSrc());
        }
        
        return cartItem;


    }


    // ----------------- 結帳用功能 -----------------

    //加入購物車前使用，檢查票數夠不夠

    public boolean checkStock(Integer activityId, Integer quantity){

        ActivityVO activity = activityRepo.findById(activityId).orElse(null);
        if (activity == null) return false;


        // 活動裡面每一張票的庫存用 ticketService的checkStock檢查庫存
        for(ActivityDetailVO detail : activity.getActivityDetails()){

            Integer ticketId = detail.getTicket().getTicketId();

            if(!ticketService.checkStock(ticketId, quantity)){

                return false;
            }

        }

        return true;

    }

    // 結帳用的，扣庫存與建立定單綁定(同樣拿ticketService.buyTicketSerial來用)

    @Transactional
    public void buyActivity(Integer activityId, 
                            Integer quantity, 
                            Integer custId, 
                            Integer orderId){

       ActivityVO activity = activityRepo.findById(activityId)
                                          .orElseThrow(() -> new RuntimeException("找不到該活動" + activityId));


       // 我自己的Activity的有效期限設計不一樣 在使用 ticketService.buyTicketSerial的方法第二個參數需要設定有效期限
       // 這邊將我之前表格的設計邏輯直接帶入 買的當下再加上我的ExpiryDate(int 然後是天數)

       long currentTimeMillis = System.currentTimeMillis();
       long validDaysInMillis = activity.getExpiryDate() * 24L * 60L * 60L * 1000L; 
       
       // 這邊貫徹我的邏輯 買的當下天數+我設定的天數 = 這個活動所有票卷的有效期限
       Timestamp expiryTimestamp = new Timestamp(currentTimeMillis + validDaysInMillis);

       // 用forEach 買下所有的票並使用ticketService裡面的方法buyTicketSerial 
       // 綁定會員、訂單，並將狀態改為「已售出/未使用」status = 2

       for (ActivityDetailVO detail : activity.getActivityDetails()){

        Integer ticketId = detail.getTicket().getTicketId();

            for(int i = 0; i < quantity; i++){

                ticketService.buyTicketSerial(ticketId, expiryTimestamp, custId, orderId);


            }
       }
                             


    }

    


}
