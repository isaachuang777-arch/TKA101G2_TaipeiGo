package com.taipeigo.activity.model;

import java.io.File;
import java.io.IOException;
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
import com.taipeigo.ticket.model.TicketVO;

@Service
public class ActivityService {

    private final ActivityRepository activityRepo;
    private final ActivityJDBCDAO activityJDBCDAO;
    private final ActivityCateRepository activityCateRepo;

    @Autowired
    public ActivityService(ActivityRepository activityRepo, ActivityJDBCDAO activityJDBCDAO,
            ActivityCateRepository activityCateRepo) {

        this.activityRepo = activityRepo;
        this.activityJDBCDAO = activityJDBCDAO;
        this.activityCateRepo = activityCateRepo;

    }

    // 傳送商品到購物車用的DTO
    public CartItemDTO getActivityCartItem(Integer activityId, String ticketType) {

        Optional<ActivityVO> box = activityRepo.findById(activityId);

        if (box.isEmpty()) {

            throw new RuntimeException("找不到該活動 " + activityId);
        }

        ActivityVO activity = box.get();

        int totalPrice = 0;

        String typeName = "";

        for (ActivityDetailVO detail : activity.getActivityDetails()) {

            if (detail.getTicket().getAvailableSerialCount() < 1) {

                throw new IllegalArgumentException("活動包含的某些門票已售完，無法加入購物車！");

            }

            switch (ticketType.toUpperCase()) {

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
                    typeName = "全票";
                    break;

                default:
                    throw new IllegalArgumentException("未知的票種" + ticketType);

            }

        }

        int finalPrice = totalPrice - activity.getDiscount();

        if (finalPrice < 0)
            finalPrice = 0;

        CartItemDTO cartItem = new CartItemDTO();
        cartItem.setPrice(finalPrice);
        cartItem.setProductId(activityId);
        cartItem.setProductName(activity.getActivityName() + " - " + typeName);
        cartItem.setQuantity(1); // 預設為一張
        cartItem.setProductType("ACTIVITY");

        return cartItem;

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

    public void addActivity(ActivityVO activity, List<Integer> ticketIds, MultipartFile[] images) {

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

        // 處理多張圖片上傳

        List<ActivityImageVO> imgList = new ArrayList<>();

        if (images != null && images.length > 0 && !images[0].isEmpty()) {

            for (MultipartFile file : images) {

                if (!file.isEmpty()) {

                    String fileUrl = saveUploadedFile(file);

                    ActivityImageVO imageVO = new ActivityImageVO();

                    imageVO.setActivity(activity);
                    imageVO.setActivityImageSrc(fileUrl);

                    imgList.add(imageVO);

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

            String newFileName = System.currentTimeMillis() + extension;

            File dest = new File(getUploadDir() + newFileName);

            file.transferTo(dest);

            return "/images/activity/" + newFileName;

        } catch (IOException ie) {

            throw new RuntimeException("圖片上傳失敗" + ie.getMessage());
        }
    }

    // ----------------- 後臺修改 -----------------

    public void updateActivity(Integer activityId, ActivityVO updatedActivity,
            List<Integer> newTicketId, MultipartFile[] newImages) {

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

            existActivityVO.getActivityImage().clear();

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

        activityRepo.save(existActivityVO);

    }

    // ----------------- 前台取得所有啟用中的分類 -----------------
    public List<ActivityCateVO> getAllActiveCategories() {

        return activityCateRepo.findAllActiveCategories();
    }

}
