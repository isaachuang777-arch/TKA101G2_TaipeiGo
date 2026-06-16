package com.taipeigo.admin.model;

import java.io.Serializable;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

//############
//21: SuperAdmin
//22: 訂單部
//23: 客服
//24: IT
//############
@Entity
@Table(name = "ADM_FUNCTION")
public class AdmFuncVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AI
    @Column(name = "FUNC_ID", updatable = false)
    private Integer funcId;

    @Column(name = "FUNC_NAME")
    private String funcName;

    public AdmFuncVO() {
        super();

    }

    // ------------------FK
    @OneToMany(mappedBy = "admfuncVO", fetch = FetchType.EAGER)
    private Set<AdmPerVO> admPerVOs;
    // -----------------FK

    public Integer getFuncId() {
        return funcId;
    }

    public void setFuncId(Integer funcId) {
        this.funcId = funcId;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public Set<AdmPerVO> getAdmPerVOs() {
        return admPerVOs;
    }

    public void setAdmPerVOs(Set<AdmPerVO> admPerVOs) {
        this.admPerVOs = admPerVOs;
    }

    @Override
    public String toString() {
        return "AdmFuncVO [funcId=" + funcId + ", funcName=" + funcName + "]";
    }

}
