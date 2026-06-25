package com.taipeigo.cs.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CsRepository extends JpaRepository<CsVO, Integer	> {

	//模糊搜尋Customer for List<CsVO>
	//模糊search
    List<CsVO> findByCustomerVO_CustNameContainingOrCustomerVO_CustAccountContaining(String keyword1, String keyword2);

	List<CsVO> findByCustomerVO_CustId(Integer custId);
	//找該用戶的case bycaseStatus [for closed ticket]
	List<CsVO> findByCustomerVO_CustIdAndCaseStatus(Integer custId, Byte caseStatus);
	//找該用戶的case bycaseStatus [for still not closed ticket]
	List<CsVO> findByCustomerVO_CustIdAndCaseStatusNot(Integer custId, Byte caseStatus);
	
	//以問題類別分類的總數
	Long countByCaseCate(Byte caseCate);
	//以狀態類別分類的總數
	Long countByCaseStatus(Byte caseStatus);
	//[結案]狀態+結案在XX 的總數
	Long countByCaseStatusAndResolvedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	
	
	//問題類別分類的列表
	List<CsVO> findByCaseCate(Byte caseCate);
	//狀態類別分類的列表
	List<CsVO> findByCaseStatus(Byte caseStatus);
	//[結案]狀態+結案在XX 的列表
	List<CsVO> findByCaseStatusAndResolvedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	//最新緊急案件列表(條件: 24小時內 還是new的)
	@Query("SELECT c FROM CsVO c WHERE c.caseStatus = :caseStatus AND c.createdAt > :time")
    List<CsVO> findByCaseStatusAndCreatedAtAfter(Byte caseStatus, java.sql.Timestamp time);
	//非結案狀態的列表 !=3
	List<CsVO> findByCaseStatusNot(Byte caseStatus);


}
