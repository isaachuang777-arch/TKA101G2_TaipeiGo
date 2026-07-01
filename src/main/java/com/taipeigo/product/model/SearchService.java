package com.taipeigo.product.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.activity.model.ActivityRepository;
import com.taipeigo.activity.model.ActivityVO;
import com.taipeigo.product.dto.SearchResultDTO;
import com.taipeigo.ticket.model.TicketRepository;
import com.taipeigo.ticket.model.TicketVO;

@Service
public class SearchService {

    private final TicketRepository ticketRepository;
    private final ActivityRepository activityRepository;

    @Autowired
    public SearchService(TicketRepository ticketRepository, ActivityRepository activityRepository) {

        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
    }

    public List<SearchResultDTO> globalSearch(String keyword, Integer minPrice, Integer maxPrice, Integer categoryId) {

        List<SearchResultDTO> resultList = new ArrayList<>();

        // 處理門票VO轉成搜尋結果的DTO

        List<TicketVO> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getTicketStatus() != null && t.getTicketStatus() == 1)
                .filter(t -> {

                    // 各種模糊查詢包含名稱敘述標籤名稱
                    boolean matchName = t.getTicketName() != null && t.getTicketName().contains(keyword);
                    boolean matchDesc = t.getTicketDescription() != null && t.getTicketDescription().contains(keyword);
                    boolean matchAddr = t.getTicketAddress() != null && t.getTicketAddress().contains(keyword);
                    boolean matchCategory = t.getTicketCategories() != null && t.getTicketCategories().stream()
                            .anyMatch(c -> c.getTicketCategoryName() != null
                                    && c.getTicketCategoryName().contains(keyword));

                    boolean matchKeyword = matchName || matchDesc || matchAddr || matchCategory;

                    // 價格區間判斷

                    boolean matchPrice = true;

                    if (minPrice != null && t.getAdultPrice() != null && t.getAdultPrice() < minPrice) {
                        matchPrice = false;
                    }

                    if (maxPrice != null && t.getAdultPrice() != null && t.getAdultPrice() > maxPrice) {
                        matchPrice = false;
                    }

                    // 分類ID判斷

                    boolean matchCategoryId = true;

                    if (categoryId != null) {
                        if (t.getTicketCategories() == null || t.getTicketCategories().isEmpty()) {
                            matchCategoryId = false;
                        } else {

                            matchCategoryId = t.getTicketCategories().stream()
                                    .anyMatch(c -> c.getTicketCategoryId().equals(categoryId));
                        }

                    }

                    return matchKeyword && matchPrice && matchCategoryId;

                }).collect(Collectors.toList());

        for (TicketVO t : tickets) {

            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(t.getTicketId());
            dto.setType("TICKET");
            dto.setTitle(t.getTicketName());
            dto.setDescription(t.getTicketDescription());
            dto.setPrice(t.getAdultPrice());

            // 把門票分類所有的ID取出來
            if (t.getTicketCategories() != null) {

                List<Integer> cateId = t.getTicketCategories().stream().map(c -> c.getTicketCategoryId())
                        .collect(Collectors.toList());

                dto.setCategoryIds(cateId);

            }

            if (t.getTicketImages() != null && !t.getTicketImages().isEmpty()) {

                dto.setImageUrl(t.getTicketImages().get(0).getTicketImageSrc());
            } else {

                dto.setImageUrl("");

            }

            resultList.add(dto);

        }

        // 處理活動VO轉成搜尋結果的DTO

        List<ActivityVO> activities = activityRepository.findAll().stream()
                .filter(a -> a.getActivityStatus() != null && a.getActivityStatus() == 1)
                .filter(a -> {

                    // 各種模糊查詢包含名稱敘述標籤名稱 - 活動

                    boolean matchName = a.getActivityName() != null && a.getActivityName().contains(keyword);
                    boolean matchDesc = a.getActivityDesc() != null && a.getActivityDesc().contains(keyword);

                    boolean matchCategory = a.getActivityCateInfoVO() != null && a.getActivityCateInfoVO().stream()
                                             .anyMatch(info -> info.getActivityCate().getCateName() != null
                                             && info.getActivityCate().getCateName().contains(keyword));

                    boolean matchKeyword = matchName || matchDesc || matchCategory;

                    // 價格區間判斷

                    int totalAdultPrice = 0;
                    if(a.getActivityDetails() != null){
                        totalAdultPrice = a.getActivityDetails().stream().mapToInt(detail -> (detail.getTicket() != null
                                            && detail.getTicket().getAdultPrice() != null) ? detail.getTicket().getAdultPrice() : 0).sum();                    
                    }

                    int discount = (a.getDiscount() != null) ? a.getDiscount() : 0;
                    int finalPrice = totalAdultPrice - discount;
                    if (finalPrice < 30) {
                        finalPrice = 30;
                    }

                    boolean matchPrice = true;

                    if (minPrice != null && finalPrice < minPrice) matchPrice = false;
                    if (maxPrice != null && finalPrice > maxPrice) matchPrice = false;

                    // 分類ID判斷
                    boolean matchCategoryId = true;
                    if(categoryId != null) {

                        if (a.getActivityDetails() == null || a.getActivityDetails().isEmpty()) {
                           
                            matchCategoryId = false;

                        } else {

                            matchCategoryId = a.getActivityDetails().stream()
                                .filter(detail -> detail.getTicket() != null && detail.getTicket().getTicketCategories() != null)
                                .flatMap(detail -> detail.getTicket().getTicketCategories().stream())
                                .anyMatch(c -> c.getTicketCategoryId().equals(categoryId));
                        }

                        
                    }

                    return matchKeyword && matchPrice && matchCategoryId;

                }).collect(Collectors.toList());

        // 只顯示一班成人票的加總 - 折扣

        for (ActivityVO a : activities) {

            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(a.getActivityId());
            dto.setType("ACTIVITY"); // 標記為活動
            dto.setTitle(a.getActivityName());
            dto.setDescription(a.getActivityDesc());

            if (a.getActivityDetails() != null) {

                List<Integer> cateIds = a.getActivityDetails().stream()
                        .filter(detail -> detail.getTicket() != null
                                && detail.getTicket().getTicketCategories() != null)
                        .flatMap(detail -> detail.getTicket().getTicketCategories().stream())
                        .map(c -> c.getTicketCategoryId()).distinct().collect(Collectors.toList());

                dto.setCategoryIds(cateIds);

            }

            int totalAdultPrice = a.getActivityDetails().stream()
                    .mapToInt(detail -> {
                        return (detail.getTicket() != null && detail.getTicket().getAdultPrice() != null)
                                ? detail.getTicket().getAdultPrice()
                                : 0;
                    })
                    .sum();

            int discount = (a.getDiscount() != null) ? a.getDiscount() : 0;
            int finalPrice = totalAdultPrice - discount;
            if (finalPrice < 30) {
                finalPrice = 30;
            }

            dto.setPrice(finalPrice);

            if (a.getActivityImage() != null && !a.getActivityImage().isEmpty()) {
                dto.setImageUrl(a.getActivityImage().get(0).getActivityImageSrc());
            } else {
                dto.setImageUrl("");
            }

            resultList.add(dto);
        }

        return resultList;
    }

}
