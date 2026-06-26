package com.taipeigo.cs.model;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsRepository extends JpaRepository<CsVO, Integer	> {
	//找findAllBy使用者->目前沒使用
	List<CsVO> findByCustomerVO_CustId(Integer custId);
	//找該用戶的case bycaseStatus [for closed ticket]
	List<CsVO> findByCustomerVO_CustIdAndCaseStatus(Integer custId, Byte caseStatus);
	//找該用戶的case bycaseStatus [for still not closed ticket]
	List<CsVO> findByCustomerVO_CustIdAndCaseStatusNot(Integer custId, Byte caseStatus);
	
	//====前台相關: 分頁====
	//找該用戶的case bycaseStatus [for closed ticket] [分頁]
	Page<CsVO> findByCustomerVO_CustIdAndCaseStatus(Integer custId, Byte caseStatus, Pageable pageable);
	//找該用戶的case bycaseStatus [for still not closed ticket][分頁]
	Page<CsVO> findByCustomerVO_CustIdAndCaseStatusNot(Integer custId, Byte caseStatus, Pageable pageable);	
	
	
	//====以上前台 以下後台
		
	//以問題類別分類的總數
	Long countByCaseCate(Byte caseCate);
	//以狀態類別分類的總數
	Long countByCaseStatus(Byte caseStatus);
	//[結案]狀態+結案在XX 的總數
	Long countByCaseStatusAndResolvedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	
	//模糊搜尋Customer for List<CsVO>
    List<CsVO> findByCustomerVO_CustNameContainingOrCustomerVO_CustAccountContaining(String keyword1, String keyword2);
	//問題類別分類的列表
	List<CsVO> findByCaseCate(Byte caseCate);
	//狀態類別分類的列表
	List<CsVO> findByCaseStatus(Byte caseStatus);
	//[結案]狀態+結案在XX(時間) 的列表 <-目前沒有使用
	List<CsVO> findByCaseStatusAndResolvedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	//最新緊急案件列表(條件: 24小時內 還是new的)
    List<CsVO> findByCaseStatusAndCreatedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	//非結案狀態的列表 !=3
	List<CsVO> findByCaseStatusNot(Byte caseStatus);

	//====分頁====
	//模糊搜尋Customer[分頁]
	Page<CsVO> findByCustomerVO_CustNameContainingOrCustomerVO_CustAccountContaining(String keyword1, String keyword2, Pageable pageable);
	//問題類別分類的列表 [分頁]
	Page<CsVO> findByCaseCate(Byte caseCate, Pageable pageable);
	//狀態類別分類的列表 [分頁]
	Page<CsVO> findByCaseStatus(Byte caseStatus, Pageable pageable);	
	//最新緊急案件列表(條件: 24小時內 還是new的) [分頁]
    Page<CsVO> findByCaseStatusAndCreatedAtAfter(Byte caseStatus, java.sql.Timestamp time, Pageable pageable);
	//非結案狀態的列表 !=3 [分頁]
	Page<CsVO> findByCaseStatusNot(Byte caseStatus, Pageable pageable);
	//全查 [分頁]
	Page <CsVO> findAll(Pageable pageable);
}
