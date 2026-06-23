package com.taipeigo.cs.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsMsgRepository extends JpaRepository<CsMsgVO, Integer> {
	//前台使用者 撈出Msg 不會撈後台備註
	List<CsMsgVO> findByCsVO_CsIdAndSenderTypeNot(Integer csId, Byte senderType);
}
