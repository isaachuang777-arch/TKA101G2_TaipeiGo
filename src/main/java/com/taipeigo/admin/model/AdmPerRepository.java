package com.taipeigo.admin.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.taipeigo.admin.model.AdmPerVO;

@Repository
public interface AdmPerRepository extends JpaRepository<AdmPerVO, Integer> {

    @Modifying
    void deleteByAdminVO_AdmId(Integer admId);
    //每個權限的人數
    long countByAdmfuncVO_FuncId(Integer funcId);
}
