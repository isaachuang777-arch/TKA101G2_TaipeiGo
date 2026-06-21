package com.taipeigo.activity.model;

import java.io.Serializable;
import com.taipeigo.ticket.model.TicketVO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ACTIVITY_DETAIL")
public class ActivityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTIVITY_DETAIL_ID")
    private Integer activityDetailId;

    @ManyToOne
    @JoinColumn(name = "ACTIVITY_ID", nullable = false)
    private ActivityVO activity;

    @ManyToOne
    @JoinColumn(name = "TICKET_ID", nullable = false)
    private TicketVO ticket;

    @Column(name = "SEQUENCE", nullable = false)
    private Integer sequence;

    public ActivityDetailVO() {
    }

    public Integer getActivityDetailId() {
        return activityDetailId;
    }

    public void setActivityDetailId(Integer activityDetailId) {
        this.activityDetailId = activityDetailId;
    }

    public ActivityVO getActivity() {
        return activity;
    }

    public void setActivity(ActivityVO activity) {
        this.activity = activity;
    }

    public TicketVO getTicket() {
        return ticket;
    }

    public void setTicket(TicketVO ticket) {
        this.ticket = ticket;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

}
