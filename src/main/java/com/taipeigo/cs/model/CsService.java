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
	public List<CsVO> findBycustId(Integer custId) {
		return csRepository.findByCustomerVO_CustId();
	}
	
	@Transactional
	//createTicket
	public CsVO createTicket(Integer custId, Byte caseCate, String msg, String msgImgsrc) {
	//who create this ticket
		CustomerVO customerVO = new CustomerVO();
		customerVO.setCustId(custId);
	//Create Cs for the ticket
		CsVO csVO = new CsVO();
		csVO.setCustomerVO(customerVO);
		csVO.setCaseCate(caseCate);
		csVO.setCaseStatus(CsVO.SsCreated);
		
	//the firstMsg
		CsMsgVO csMsgVO= new CsMsgVO();
		csMsgVO.setCsVO(csVO);
		csMsgVO.setCustomerVO(customerVO);
		csMsgVO.setSenderType(CsMsgVO.Srcust);
		csMsgVO.setMsgContent(msg);
		if(msgImgsrc != null) {
			csMsgVO.setMsgImgsrc(msgImgsrc);
		}
	
	//Save to DB
		Set<CsMsgVO> msgSet =new  HashSet<>();
		msgSet.add(csMsgVO);
		
		csVO.setCsMsgVOs(msgSet);
		
		csRepository.save(csVO);
		
		return csVO;
		
		
	
	}
}
