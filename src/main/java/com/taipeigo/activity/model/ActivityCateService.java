package com.taipeigo.activity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class ActivityCateService {

    private final ActivityCateRepository cateRepository;
    private final ActivityJDBCDAO activityJDBCDAO;
    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityCateService(ActivityCateRepository cateRepository, ActivityJDBCDAO activityJDBCDAO, ActivityRepository activityRepository) {

        this.cateRepository = cateRepository;
        this.activityJDBCDAO = activityJDBCDAO;
        this.activityRepository = activityRepository;

    }

    public List<ActivitySectionDTO> getHomeSections() {

        List<ActivitySectionDTO> sections = new ArrayList<>();

        // 用fillter找到啟用狀態為1的分類 然後存在一個集合
        List<ActivityCateVO> activeCategories = cateRepository.findAll().stream()
                .filter(cate -> cate.getIsActive() == 1).collect(Collectors.toList());

        // 用Collections的方法 shuffle 打亂分類順序並取出第一個當作這次的分類給第一列用的
        if (!activeCategories.isEmpty()) {

            Collections.shuffle(activeCategories);

            ActivityCateVO randomCate = activeCategories.get(0);

            String SectionTitle = "最新活動: " + randomCate.getCateName();

            MultiValueMap<String, String> querMap = new LinkedMultiValueMap<>();

            querMap.add("cateId", String.valueOf(randomCate.getActivityCateId()));

            // 用寫好的JDBC去撈

            List<ActivityVO> randomCateActivities = activityJDBCDAO.getSearch(querMap, true);

            if (randomCateActivities != null && !randomCateActivities.isEmpty()) {

                int limit = Math.min(randomCateActivities.size(), 3);

                sections.add(new ActivitySectionDTO(SectionTitle, randomCateActivities.subList(0, limit)));

            }
        }

            // 一般活動不綁活動類型顯示

            MultiValueMap<String, String> emptyMap = new LinkedMultiValueMap<>();

            List<ActivityVO> allActivities = activityJDBCDAO.getSearch(emptyMap, true);

            if (allActivities != null && !allActivities.isEmpty()) {

                // 找出最便宜

                List<ActivityVO> sortedByPrice = new ArrayList<>(allActivities);

                sortedByPrice.sort(Comparator.comparing(ActivityVO::getAdultPrice));

                int priceLimit = Math.min(sortedByPrice.size(), 3);
                sections.add(new ActivitySectionDTO("最佳優惠", sortedByPrice.subList(0, priceLimit)));

                // 準備給懶的規劃那區域的(現在改為從資料庫讀取「首頁推薦」標記的活動)
                MultiValueMap<String, String> recMap = new LinkedMultiValueMap<>();
                recMap.add("isRecommended", "1");
                recMap.add("pageSize", "100"); // 確保抓出所有推薦活動再來洗牌
                List<ActivityVO> recommendedActivities = activityJDBCDAO.getSearch(recMap, true);
                
                // 如果後台還沒設定任何推薦活動，就退回原本的「亂數抽 3 筆」作為 fallback
                if (recommendedActivities == null || recommendedActivities.isEmpty()) {
                    List<ActivityVO> shuffledActivities = new ArrayList<>(allActivities);
                    Collections.shuffle(shuffledActivities);
                    int randomLimit = Math.min(shuffledActivities.size(), 3);
                    sections.add(new ActivitySectionDTO("懶得規劃?", shuffledActivities.subList(0, randomLimit)));
                } else {
                    // 將設定為推薦的活動也洗牌，這樣如果超過 3 筆，每次都會隨機挑選 3 筆！
                    Collections.shuffle(recommendedActivities);
                    int limit = Math.min(recommendedActivities.size(), 3);
                    sections.add(new ActivitySectionDTO("懶得規劃?", recommendedActivities.subList(0, limit)));
                }

            }

        return sections;
    }


    // --------------活動分類管理---------------

    public List<ActivityCateVO> getAllCategories(){
        return cateRepository.findAll();
    }

    public ActivityCateVO  saveCategory(ActivityCateVO cate){

        if(cate.getActivityCateId() == null){
            cate.setIsActive(1);
        } else {
            // 防呆機制：如果是修改分類，且沒有上傳新圖片，保留舊圖片
            if (cate.getCateIcon() == null || cate.getCateIcon().length == 0) {
                ActivityCateVO oldCate = cateRepository.findById(cate.getActivityCateId()).orElse(null);
                if (oldCate != null && oldCate.getCateIcon() != null) {
                    cate.setCateIcon(oldCate.getCateIcon());
                }
            }
        }

        return cateRepository.save(cate);
    }


    public ActivityCateVO toggleCateStatus(Integer cateId){

        ActivityCateVO cate = cateRepository.findById(cateId).orElse(null);
        
        if(cate != null){

            cate.setIsActive(cate.getIsActive() == 1 ? 0 : 1);

            cateRepository.save(cate);
        }

        return cate;
    }
}
    




