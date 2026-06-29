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

    @Autowired
    public ActivityCateService(ActivityCateRepository cateRepository, ActivityJDBCDAO activityJDBCDAO) {

        this.cateRepository = cateRepository;
        this.activityJDBCDAO = activityJDBCDAO;

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

            // 處理分類名稱用(只取斜線之前)

            String oldName = randomCate.getCateName();
            String newName = oldName.contains("/") ? oldName.split("/")[0].trim() : oldName;

            String SectionTitle = "最新活動: " + newName;

            MultiValueMap<String, String> querMap = new LinkedMultiValueMap<>();

            querMap.add("cateId", String.valueOf(randomCate.getActivityCateId()));

            // 用寫好的JDBC去撈

            List<ActivityVO> randomCateActivities = activityJDBCDAO.getSearch(querMap, true);

            if (randomCateActivities != null && !randomCateActivities.isEmpty()) {

                int limit = Math.min(randomCateActivities.size(), 3);

                sections.add(new ActivitySectionDTO(SectionTitle, randomCateActivities.subList(0, limit)));

            }

            // (為了找出最佳優惠跟隨機活動)先撈出全部已上架的活動

            MultiValueMap<String, String> emptyMap = new LinkedMultiValueMap<>();

            List<ActivityVO> allActivities = activityJDBCDAO.getSearch(emptyMap, true);

            if (allActivities != null && !allActivities.isEmpty()) {

                // 找出最便宜

                List<ActivityVO> sortedByPrice = new ArrayList<>(allActivities);

                sortedByPrice.sort(Comparator.comparing(ActivityVO::getAdultPrice));

                int priceLimit = Math.min(sortedByPrice.size(), 3);
                sections.add(new ActivitySectionDTO("最佳優惠", sortedByPrice.subList(0, priceLimit)));

                // 準備給懶的規劃那區域的(所有活動亂數)

                List<ActivityVO> shuffledActivities = new ArrayList<>(allActivities);

                // 把全站活動洗牌
                Collections.shuffle(shuffledActivities);
                int randomLimit = Math.min(shuffledActivities.size(), 3);
                sections.add(new ActivitySectionDTO("懶得規劃?", shuffledActivities.subList(0, randomLimit)));

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
