package com.taipeigo.ticket.model;

import java.util.ArrayList;
import java.util.List;
import com.taipeigo.ticketcategory.model.TicketCategoryDTO;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;

public class TicketDTO {
    private Integer ticketId;
    private String ticketName;
    private String ticketDescription;
    private String ticketAddress;
    private Integer adultOriginalPrice;
    private Integer adultPrice;
    private Integer childOriginalPrice;
    private Integer childPrice;
    private Integer concessionOriginalPrice;
    private Integer concessionPrice;
    private List<String> imageUrls;
    private List<TicketCategoryDTO> categories;
    private Long availableSerialCount;

    public TicketDTO() {
    }

    public static TicketDTO fromEntity(TicketVO vo) {
        if (vo == null) {
            return null;
        }
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(vo.getTicketId());
        dto.setTicketName(vo.getTicketName());
        dto.setTicketDescription(vo.getTicketDescription());
        dto.setTicketAddress(vo.getTicketAddress());
        dto.setAdultOriginalPrice(vo.getAdultOriginalPrice());
        dto.setAdultPrice(vo.getAdultPrice());
        dto.setChildOriginalPrice(vo.getChildOriginalPrice());
        dto.setChildPrice(vo.getChildPrice());
        dto.setConcessionOriginalPrice(vo.getConcessionOriginalPrice());
        dto.setConcessionPrice(vo.getConcessionPrice());

        // === 處理圖片 ===
        List<String> urls = new ArrayList<>();
        if (vo.getTicketImages() != null) {
            for (TicketImageVO img : vo.getTicketImages()) {
                urls.add(img.getTicketImageSrc());
            }
        }
        dto.setImageUrls(urls);

        // === 處理分類 ===
        // 只顯示 CategoryStatus == 1 的票種 （有啟用的分類）
        List<TicketCategoryDTO> categories = new ArrayList<>();
        if (vo.getTicketCategories() != null) {
            for (TicketCategoryVO cate : vo.getTicketCategories()) {
                if (cate.getTicketCategoryStatus() != null && cate.getTicketCategoryStatus() == 1) {
                    categories.add(new TicketCategoryDTO(cate.getTicketCategoryId(), cate.getTicketCategoryName()));
                }
            }
        }
        dto.setCategories(categories);

        dto.setAvailableSerialCount(vo.getAvailableSerialCount());

        return dto;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public void setTicketDescription(String ticketDescription) {
        this.ticketDescription = ticketDescription;
    }

    public String getTicketAddress() {
        return ticketAddress;
    }

    public void setTicketAddress(String ticketAddress) {
        this.ticketAddress = ticketAddress;
    }

    public Integer getAdultOriginalPrice() {
        return adultOriginalPrice;
    }

    public void setAdultOriginalPrice(Integer adultOriginalPrice) {
        this.adultOriginalPrice = adultOriginalPrice;
    }

    public Integer getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(Integer adultPrice) {
        this.adultPrice = adultPrice;
    }

    public Integer getChildOriginalPrice() {
        return childOriginalPrice;
    }

    public void setChildOriginalPrice(Integer childOriginalPrice) {
        this.childOriginalPrice = childOriginalPrice;
    }

    public Integer getChildPrice() {
        return childPrice;
    }

    public void setChildPrice(Integer childPrice) {
        this.childPrice = childPrice;
    }

    public Integer getConcessionOriginalPrice() {
        return concessionOriginalPrice;
    }

    public void setConcessionOriginalPrice(Integer concessionOriginalPrice) {
        this.concessionOriginalPrice = concessionOriginalPrice;
    }

    public Integer getConcessionPrice() {
        return concessionPrice;
    }

    public void setConcessionPrice(Integer concessionPrice) {
        this.concessionPrice = concessionPrice;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<TicketCategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<TicketCategoryDTO> categories) {
        this.categories = categories;
    }

    public Long getAvailableSerialCount() {
        return availableSerialCount;
    }

    public void setAvailableSerialCount(Long availableSerialCount) {
        this.availableSerialCount = availableSerialCount;
    }
}
