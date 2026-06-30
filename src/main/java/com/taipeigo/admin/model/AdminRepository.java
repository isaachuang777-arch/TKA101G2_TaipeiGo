package com.taipeigo.admin.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<AdminVO, Integer> {
    // 用admAcc檢查有沒有重複->create前置行為
    AdminVO findByadmAcc(String admAcc);

    // 用帳號狀態admStatus找人(List)
    List<AdminVO> findByadmStatus(Byte admStatus);

    // 登入前置作業[SpringBoot已接管登入]
    // AdminVO findByAdmAccAndAdmPw(String admAcc, String admPw);

    //模糊search
    List<AdminVO> findByAdmAccContainingOrAdmNameContaining(String keyword1, String keyword2);
    
    //找出XXX權限的管理員 [如果用JPA會太長 所以直接HQL語法]
    @Query("SELECT DISTINCT a FROM AdminVO a JOIN a.admPerVOs p WHERE p.admfuncVO.funcId = ?1")
    List<AdminVO> findByAdminByFuncId(Integer funcId);
    
    // 找沒權限的管理員
    List<AdminVO> findByAdmPerVOsIsEmpty();

    // 沒權限人數
    long countByAdmPerVOsIsEmpty();
    
    /* ----分頁功能以下 ----*/

    // 用帳號狀態admStatus找人(分頁)
    Page<AdminVO> findByadmStatus(Byte admStatus, Pageable pageable);

    //模糊searchName/acc(分頁)
    Page<AdminVO> findByAdmAccContainingOrAdmNameContaining(String keyword1, String keyword2, Pageable pageable);

    //找出XXX權限的管理員 [分頁]
    Page<AdminVO> findDistinctByAdmPerVOs_AdmfuncVO_FuncId(Integer funcId, Pageable pageable);
    
    // 找沒權限的管理員(分頁)
    Page<AdminVO> findByAdmPerVOsIsEmpty(Pageable pageable);

}
