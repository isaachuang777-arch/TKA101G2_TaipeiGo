package com.taipeigo.admin.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Set;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ADMIN")

public class AdminVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AI
    @Column(name = "ADM_ID", updatable = false)
    private Integer admId;

    @NotNull(message = "帳號不可為空")
    @Length(min = 4, max = 20, message = "失敗:帳號只能包含4位或以上的英文、數字或底線！　\n")
    @Column(name = "ADM_ACC")
    private String admAcc;
    
    @NotNull(message = "密碼不可為空")
    @Length(min = 8, message = "失敗：密碼必須為 8 碼以上的英文、數字或符號！　\n")
    @Column(name = "ADM_PW")
    private String admPw;
    
    @NotNull(message = "姓名不可為空")
    @Length(min = 2, max = 10, message = "失敗：姓名長度需為2~10位！　\n")
    @Column(name = "ADM_NAME")
    private String admName;

    @Column(name = "HIREDATE")
    private Date hireDate;

    @Column(name = "ADM_STATUS")
    private Byte admStatus; 
///////////////////////////////////////
    public static final Byte StatusEnabled = 1;             // 啟用
    public static final Byte StatusDisabled  = 0;            // 停用
    public static final Byte StatusForcetoChangePW = 9;     // 強制換密碼
//////////////////////可以寫成adminVO.setAdmStatus(AdminVO.StatusEnabled);

    // ------------------FK
    @OneToMany(mappedBy = "adminVO", fetch = FetchType.EAGER)
    @OrderBy("admfuncVO ") //權限排序才不會亂跳
    private Set<AdmPerVO> admPerVOs;
    // -----------------FK

    public AdminVO() {
        super();
    }

    public Integer getAdmId() {
        return admId;
    }

    public void setAdmId(Integer admId) {
        this.admId = admId;
    }

    public String getAdmAcc() {
        return admAcc;
    }

    public void setAdmAcc(String admAcc) {
        this.admAcc = admAcc;
    }

    public String getAdmPw() {
        return admPw;
    }

    public void setAdmPw(String admPw) {
        this.admPw = admPw;
    }

    public String getAdmName() {
        return admName;
    }

    public void setAdmName(String admName) {
        this.admName = admName;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;

    }

    public Byte getAdmStatus() {
        return admStatus;
    }

    public void setAdmStatus(Byte admStatus) {
        this.admStatus = admStatus;
    }

    public Set<AdmPerVO> getAdmPerVO() {
        return admPerVOs;
    }

    public void setAdmPerVO(Set<AdmPerVO> admPerVOs) {
        this.admPerVOs = admPerVOs;
    }

    @Override
    public String toString() {
        return "AdminVO [admId=" + admId + ", admAcc=" + admAcc + ", admPw=" + admPw + ", admName=" + admName
                + ", hireDate=" + hireDate + ", admStatus=" + admStatus + "]";
    }

}
