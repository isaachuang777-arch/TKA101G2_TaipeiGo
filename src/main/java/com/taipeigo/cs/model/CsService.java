package com.taipeigo.cs.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.transaction.Transactional;

@Service
public class CsService {
	@Autowired
	private CsRepository csRepository;
	@Autowired
	private CsMsgRepository csmsgRepository;
	
	
	
	//---------------------------------
	//後台
	//---------------------------------
	
	//getAllCs全查
	public List<CsVO> findAllCs(){
		return csRepository.findAll();
	}
	//findByCsId 找單
	public CsVO findByCsId(Integer csId) {
		return csRepository.findById(csId).orElse(null);
	}

	
	
//-------------------------
//前台
//-------------------------
	//findBycustId 以使用找案子
	public List<CsVO> findByCustId(Integer custId) {
		return csRepository.findByCustomerVO_CustId(custId);
	}
	
	@Transactional
	//createTicket
	public CsVO createTicket(Integer custId, Byte caseCate, String msg, String msgImgsrc) {
	//who create this ticket
		CustomerVO customerVO = new CustomerVO();
		customerVO.setCustId(custId);
	//Create Cs for the ticket
		CsVO csVO = new CsVO(); //新增一個CsVO
		csVO.setCustomerVO(customerVO); //設定是誰
		csVO.setCaseCate(caseCate); //問題分類
		csVO.setCaseStatus(CsVO.SsCreated); //問題狀態
		
	//the firstMsg
		CsMsgVO csMsgVO= new CsMsgVO(); //新增一個CsMsgVO
		csMsgVO.setCsVO(csVO);//設定哪個case
		csMsgVO.setCustomerVO(customerVO); //設定是誰
		csMsgVO.setSenderType(CsMsgVO.Srcust); //設定留言類型
		csMsgVO.setMsgContent(msg); //設定留言內容
		if(msgImgsrc != null) { 
			csMsgVO.setMsgImgsrc(msgImgsrc); //圖片路徑
		}
	
	//Save to DB
		Set<CsMsgVO> msgSet =new  HashSet<>();
		msgSet.add(csMsgVO);
		csVO.setCsMsgVOs(msgSet);
		
		csRepository.save(csVO);
		
		return csVO;
	}
//findByActiveCase
//是找非結案的Case|| List<CsVO> findByCustomerVO_CustIdAndCaseStatusNot(Integer custId, Byte caseStatus);
	public List<CsVO> findByActiveCases(Integer custId){
		return csRepository.findByCustomerVO_CustIdAndCaseStatusNot(custId, (byte)3);
	}
	
//findByInactiveCase
// 	List<CsVO> findByCustomerVO_CustIdAndCaseStatus(Integer custId, Byte caseStatus);
	public List<CsVO> findByInactiveCases(Integer custId){
		return csRepository.findByCustomerVO_CustIdAndCaseStatus(custId, (byte) 3);
	}
	
//	前台使用者查出Msg + 不會出現 7號Worknote : List<CsMsgVO> findByCsVO_CsIdAndSenderTypeNot(Integer csId, Byte senderType);
	public List<CsMsgVO> custFindMsg(Integer csId){
		return csmsgRepository.findByCsVO_CsIdAndSenderTypeNot(csId, (byte)7);
	}
	
//使用者的reply
	@Transactional
	public void customerreply(CsMsgVO newcsmsgVO, Integer csId) {
		
		
		//用findById(csId去)拿出一個csVO
		CsVO csVO = csRepository.findById(csId).orElseThrow();
		//先拿現在的CaseStatus
		Byte currentStatus = csVO.getCaseStatus();
		//資安防呆 不要相信前端 如果已結案 不準存回覆
		if(currentStatus == (byte) 3){
        throw new RuntimeException("此案件已結案，無法新增回覆！");
    	}
    
		//newcsmsgVO要有csId 這樣才會關聯到(因為csId 是FK)
		newcsmsgVO.setCsVO(csVO);
		newcsmsgVO.setSenderType(CsMsgVO.Srcust);
		//存新Msg
		csmsgRepository.save(newcsmsgVO);
		//Case 更改的部分 判斷:  改待回覆 CsStatus 1 / 如果目前是0狀態 reply依然是 0
		if(currentStatus== (byte)0){
			csVO.setCaseStatus(csVO.SsCreated);
			// 強制更新時間
			csVO.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		}else{
		csVO.setCaseStatus(csVO.SsPending);
		}
		//要把case/CsVO存回去~
		csRepository.save(csVO);
	}
//使用者結案button
	@Transactional
	public void customerclose(Integer csId){
		//用findById(csId去)拿出一個csVO
		CsVO csVO = csRepository.findById(csId).orElseThrow();
		//newcsmsgVO要有csId 這樣才會關聯到(因為csId 是FK)
		CsMsgVO newcsmsgVO = new CsMsgVO();
		newcsmsgVO.setCsVO(csVO);
		newcsmsgVO.setMsgContent("【系統提示】此案件已由 會員 標記為結案。");
		newcsmsgVO.setSenderType(CsMsgVO.Srsystem);
		csmsgRepository.save(newcsmsgVO);
		
		//update csVO status+resolvedtime
		csVO.setCaseStatus(CsVO.SsResovled);
		csVO.setResolvedAt(new java.sql.Timestamp(System.currentTimeMillis()));

		csRepository.save(csVO);
	}

}
