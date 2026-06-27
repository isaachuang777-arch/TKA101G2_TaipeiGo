package com.taipeigo.ticketcategory.model;

public class TicketCategoryDTO {
    private Integer ticketCategoryId;
    private String ticketCategoryName;

    public TicketCategoryDTO() {
    }

    public TicketCategoryDTO(Integer ticketCategoryId, String ticketCategoryName) {
        this.ticketCategoryId = ticketCategoryId;
        this.ticketCategoryName = ticketCategoryName;
    }

    public Integer getTicketCategoryId() {
        return ticketCategoryId;
    }

    public void setTicketCategoryId(Integer ticketCategoryId) {
        this.ticketCategoryId = ticketCategoryId;
    }

    public String getTicketCategoryName() {
        return ticketCategoryName;
    }

    public void setTicketCategoryName(String ticketCategoryName) {
        this.ticketCategoryName = ticketCategoryName;
    }
}
