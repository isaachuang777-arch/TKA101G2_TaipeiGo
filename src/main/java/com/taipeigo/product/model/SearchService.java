package com.taipeigo.product.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> globalSearch(String keyword, Integer minPrice, Integer maxPrice,
                                              List<Integer> categoryIds, String type, int page, int size, String sortBy) {

        List<SearchResultDTO> resultList = new ArrayList<>();

        // === 1. 處理門票 TICKET ===
        if ("ALL".equals(type) || "TICKET".equals(type)) {

            List<TicketVO> tickets = ticketRepository.findAll().stream()
                    .filter(t -> t.getTicketStatus() != null && t.getTicketStatus() == 1)
                    .filter(t -> {

                        boolean matchName = t.getTicketName() != null && t.getTicketName().contains(keyword);
                        boolean matchDesc = t.getTicketDescription() != null && t.getTicketDescription().contains(keyword);
                        boolean matchAddr = t.getTicketAddress() != null && t.getTicketAddress().contains(keyword);
                        boolean matchCategory = t.getTicketCategories() != null && t.getTicketCategories().stream()
                                .anyMatch(c -> c.getTicketCategoryName() != null && c.getTicketCategoryName().contains(keyword));
                        boolean matchKeyword = matchName || matchDesc || matchAddr || matchCategory;

                        boolean matchPrice = true;
                        if (minPrice != null && t.getAdultPrice() != null && t.getAdultPrice() < minPrice) matchPrice = false;
                        if (maxPrice != null && t.getAdultPrice() != null && t.getAdultPrice() > maxPrice) matchPrice = false;

                        boolean matchCategoryId = true;
                        if (categoryIds != null && !categoryIds.isEmpty()) {
                            if (t.getTicketCategories() == null || t.getTicketCategories().isEmpty()) {
                                matchCategoryId = false;
                            } else {
                                matchCategoryId = t.getTicketCategories().stream()
                                        .anyMatch(c -> categoryIds.contains(c.getTicketCategoryId()));
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
                dto.setOriginalPrice(t.getAdultOriginalPrice());

                if (t.getTicketCategories() != null) {
                    List<Integer> cateId = t.getTicketCategories().stream().map(c -> c.getTicketCategoryId()).collect(Collectors.toList());
                    dto.setCategoryIds(cateId);
                }

                if (t.getTicketImages() != null && !t.getTicketImages().isEmpty()) {
                    dto.setImageUrl(t.getTicketImages().get(0).getTicketImageSrc());
                } else {
                    dto.setImageUrl("");
                }
                resultList.add(dto);
            }
        }

        // === 2. 處理活動 ACTIVITY ===
        if ("ALL".equals(type) || "ACTIVITY".equals(type)) {
            List<ActivityVO> activities = activityRepository.findAll().stream()
                    .filter(a -> a.getActivityStatus() != null && a.getActivityStatus() == 1)
                    .filter(a -> {
                        boolean matchName = a.getActivityName() != null && a.getActivityName().contains(keyword);
                        boolean matchDesc = a.getActivityDesc() != null && a.getActivityDesc().contains(keyword);
                        boolean matchCategory = false;
                        if (a.getActivityCateInfoVO() != null) {
                            for (com.taipeigo.activity.model.ActivityCateInfoVO info : a.getActivityCateInfoVO()) {
                                try {
                                    if (info.getActivityCate() != null && info.getActivityCate().getCateName() != null && info.getActivityCate().getCateName().contains(keyword)) {
                                        matchCategory = true;
                                        break;
                                    }
                                } catch (jakarta.persistence.EntityNotFoundException e) {
                                    // 忽略
                                }
                            }
                        }
                        boolean matchKeyword = matchName || matchDesc || matchCategory;

                        int totalAdultPrice = 0;
                        if(a.getActivityDetails() != null){
                            totalAdultPrice = a.getActivityDetails().stream().mapToInt(detail -> (detail.getTicket() != null && detail.getTicket().getAdultPrice() != null) ? detail.getTicket().getAdultPrice() : 0).sum();
                        }
                        int discount = (a.getDiscount() != null) ? a.getDiscount() : 0;
                        int finalPrice = totalAdultPrice - discount;
                        if (finalPrice < 30) finalPrice = 30;

                        boolean matchPrice = true;
                        if (minPrice != null && finalPrice < minPrice) matchPrice = false;
                        if (maxPrice != null && finalPrice > maxPrice) matchPrice = false;

                        boolean matchCategoryId = true;
                        if (categoryIds != null && !categoryIds.isEmpty()) {
                            if (a.getActivityDetails() == null || a.getActivityDetails().isEmpty()) {
                                matchCategoryId = false;
                            } else {
                                matchCategoryId = a.getActivityDetails().stream()
                                        .filter(detail -> detail.getTicket() != null && detail.getTicket().getTicketCategories() != null)
                                        .flatMap(detail -> detail.getTicket().getTicketCategories().stream())
                                        .anyMatch(c -> categoryIds.contains(c.getTicketCategoryId()));
                            }
                        }

                        return matchKeyword && matchPrice && matchCategoryId;
                    }).collect(Collectors.toList());

            for (ActivityVO a : activities) {
                SearchResultDTO dto = new SearchResultDTO();
                dto.setId(a.getActivityId());
                dto.setType("ACTIVITY");
                dto.setTitle(a.getActivityName());
                dto.setDescription(a.getActivityDesc());

                if (a.getActivityDetails() != null) {
                    List<Integer> cateIds = a.getActivityDetails().stream()
                            .filter(detail -> detail.getTicket() != null && detail.getTicket().getTicketCategories() != null)
                            .flatMap(detail -> detail.getTicket().getTicketCategories().stream())
                            .map(c -> c.getTicketCategoryId()).distinct().collect(Collectors.toList());
                    dto.setCategoryIds(cateIds);
                }

                int totalAdultPrice = a.getActivityDetails() == null ? 0 : a.getActivityDetails().stream()
                        .mapToInt(detail -> (detail.getTicket() != null && detail.getTicket().getAdultPrice() != null) ? detail.getTicket().getAdultPrice() : 0).sum();

                int totalOriginalPrice = a.getActivityDetails() == null ? 0 : a.getActivityDetails().stream()
                        .mapToInt(detail -> (detail.getTicket() != null && detail.getTicket().getAdultOriginalPrice() != null) ? detail.getTicket().getAdultOriginalPrice() : 0).sum();

                int discount = (a.getDiscount() != null) ? a.getDiscount() : 0;
                int finalPrice = totalAdultPrice - discount;
                if (finalPrice < 30) finalPrice = 30;

                dto.setPrice(finalPrice);
                dto.setOriginalPrice(totalOriginalPrice);

                if (a.getActivityImage() != null && !a.getActivityImage().isEmpty()) {
                    dto.setImageUrl(a.getActivityImage().get(0).getActivityImageSrc());
                } else {
                    dto.setImageUrl("");
                }
                resultList.add(dto);
            }
        }

        // === 3. 排序 Sorting ===
        if ("priceAsc".equals(sortBy)) {
            resultList.sort(Comparator.comparing(SearchResultDTO::getPrice, Comparator.nullsLast(Integer::compareTo)));
        } else if ("priceDesc".equals(sortBy)) {
            resultList.sort(Comparator.comparing(SearchResultDTO::getPrice, Comparator.nullsLast(Integer::compareTo)).reversed());
        }

        // === 4. 後端分頁 Pagination ===
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultList.size());

        List<SearchResultDTO> subList;
        if (start <= end) {
            subList = resultList.subList(start, end);
        } else {
            subList = Collections.emptyList(); // 防止頁碼超出範圍
        }

        int currentMaxPrice = 0;
        if (!resultList.isEmpty()) {
            currentMaxPrice = resultList.stream().mapToInt(SearchResultDTO::getPrice).max().orElse(10000);
        }

        PageImpl<SearchResultDTO> pageData = new PageImpl<>(subList, pageable, resultList.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageData.getContent());
        response.put("totalPages", pageData.getTotalPages());
        response.put("totalElements", pageData.getTotalElements());
        response.put("maxPrice", currentMaxPrice);

        return response;
    }
}
