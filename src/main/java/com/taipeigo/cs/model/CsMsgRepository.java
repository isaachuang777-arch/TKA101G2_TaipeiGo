package com.taipeigo.cs.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CsMsgRepository extends JpaRepository<CsMsgVO, Integer> {
	//前台使用者 撈出Msg 不會撈後台備註
	List<CsMsgVO> findByCsVO_CsIdAndSenderTypeNot(Integer csId, Byte senderType);

	//後台管理 撈出Msg 全出
	@Query("SELECT m FROM CsMsgVO m WHERE m.csVO.csId = ?1 ORDER BY m.msgId ASC")
	List<CsMsgVO> findByCsVO_CsId(Integer csId);
}
