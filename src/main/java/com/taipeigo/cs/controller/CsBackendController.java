package com.taipeigo.cs.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.cs.model.CsMsgRepository;
import com.taipeigo.cs.model.CsMsgVO;
import com.taipeigo.cs.model.CsRepository;
import com.taipeigo.cs.model.CsService;
import com.taipeigo.cs.model.CsVO;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/backend/cs")
public class CsBackendController {
	
	@Autowired
	private CsService csService;
	@Autowired
	private CsRepository csRepository;
	@Autowired
	private CsMsgRepository csMsgRepository;
	
//後台客服首頁+dashboard
	@GetMapping({"/", "index", "dashboard"})
	public String showCsdahsboardpage(Model model) {
		//以問題類別分類的總數
		//11=操作問題, 12=訂單問題, 13=其他
		Long caseCate11=  csRepository.countByCaseCate((byte) 11);
		model.addAttribute("caseCate11", caseCate11);
		Long caseCate12=  csRepository.countByCaseCate((byte) 12);
		model.addAttribute("caseCate12", caseCate12);
		Long caseCate13=  csRepository.countByCaseCate((byte) 13);
		model.addAttribute("caseCate3", caseCate13);
		
		//以狀態類別分類的總數 Long countByCaseStatus(Byte caseStatus);
		//0=新增, 1=待處理, 2=已回覆, 3=已結案
		Long caseStatus0=  csRepository.countByCaseStatus((byte) 0);
		model.addAttribute("caseStatus0", caseStatus0);
		Long caseStatus1=  csRepository.countByCaseStatus((byte) 1);
		model.addAttribute("caseStatus1", caseStatus1);
		Long caseStatus2=  csRepository.countByCaseStatus((byte) 2);
		model.addAttribute("caseStatus2", caseStatus2);
		Long caseStatus3=  csRepository.countByCaseStatus((byte) 3);
		
		long oneDayAgoMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
		java.sql.Timestamp yesterday = new java.sql.Timestamp(oneDayAgoMillis);
		model.addAttribute("caseStatus3", caseStatus3);
		//[結案]狀態+結案在XX 的總數 Long countByCaseStatusAndResolvedAtAfter(Byte caseStatus, java.sql.Timestamp time);
		Long todayClosed = csRepository.countByCaseStatusAndResolvedAtAfter((byte)3, yesterday);
		
		//urgentCases
		List<CsVO> urgentCases = csRepository.findByCaseStatusAndCreatedAtAfter((byte) 0, yesterday);
		model.addAttribute("urgentCases", urgentCases);
		
		return "backend/cs/index";
	}
	//listAll萬用頁
	@GetMapping("/listAll")
	public String listCase(Model model,
							@RequestParam (value = "caseStatus", required = false) Byte caseStatus,
							@RequestParam (value = "caseCate", required = false) Byte caseCate,
							@RequestParam (value = "keyword", required = false) String keyword1,
							@RequestParam(value = "urgent", required = false) Boolean isUrgent,
							@RequestParam(value = "unclosed", required = false) Boolean isUnclosed
							) {
		
		List<CsVO> csList = null;
		
		if(Boolean.TRUE.equals(isUrgent)){
			long oneDayAgoMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        	java.sql.Timestamp yesterday = new java.sql.Timestamp(oneDayAgoMillis);
        	csList = csRepository.findByCaseStatusAndCreatedAtAfter((byte) 0, yesterday);
		}else if(Boolean.TRUE.equals(isUnclosed)){
			csList = csRepository.findByCaseStatusNot((byte)3);
		}else if(keyword1 != null){
			String keyword2 = keyword1;
			csList = csRepository.findByCustomerVO_CustNameContainingOrCustomerVO_CustAccountContaining(keyword1, keyword2);
		}else if(caseStatus != null) {
			csList = csRepository.findByCaseStatus(caseStatus);
		}else if (caseCate != null) {
			csList = csRepository.findByCaseCate(caseCate);
		}else {
			csList = csRepository.findAll();
		}
	    
		if (csList != null) {
	        csList.sort(java.util.Comparator.comparing(CsVO::getCreatedAt));
	    }
		
		model.addAttribute("csList", csList);
		return "backend/cs/listAll";
	}
	//讀案件
	@GetMapping("view")
	public String viewCase(@RequestParam(value = "csId", required = true) Integer csId,
							Model model) {
		CsVO csVO= csRepository.findById(csId).orElse(null);
		List<CsMsgVO> csMsgList = csMsgRepository.findByCsVO_CsId(csId);
		model.addAttribute("csVO", csVO);
		model.addAttribute("csMsgList", csMsgList);
		return "backend/cs/view";
		
	}
	//管理員回覆或內部訊息(Woknote)
		@PostMapping("/backendReply")
		public String backendCsReply(HttpSession session,
								@RequestParam("csId") Integer csId,
								@RequestParam("msg") String msg,
								@RequestParam (value = "msgImgsrc", required = false) String msgImgsrc,
								@RequestParam ("senderType") Byte senderType,
								RedirectAttributes redirectAttributes)
		{
			//1.由網頁拿資料
			//who reply
			AdminVO adminVO = (AdminVO) session.getAttribute("adminVO");
			//2.開始做打包前置作業 建一個新的Msg
			CsMsgVO csmsgVO = new CsMsgVO();
			//3.打包setter
			//存留言+who
			csmsgVO.setMsgContent(msg);
			csmsgVO.setAdminVO(adminVO);
			//TODO 上傳圖片
			try {
			//丟回Service做事
			csService.adminorworknotereply(csmsgVO , csId, senderType);
			redirectAttributes.addFlashAttribute("successMsg", "已回覆");
			}catch(RuntimeException e) {
				redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
			return "redirect:/backend/cs/index";
			}
			
		return "redirect:/backend/cs/view?csId=" + csId;
		}
	//後台結案 backendclose(Integer csId)
		@PostMapping("/backendClose")
		public String backendClosecase(HttpSession session,
						@RequestParam("csId") Integer csId,
						RedirectAttributes redirectAttributes)
		{
			AdminVO adminVO= (AdminVO) session.getAttribute("adminVO");
			try{
				csService.backendclose(csId, adminVO);
				redirectAttributes.addFlashAttribute("successMsg", "案件已成功結案!");
				return "redirect:/backend/cs/view?csId=" + csId;
			}catch(RuntimeException e) {
				redirectAttributes.addFlashAttribute("errorMsg",  "系統維護中，請稍後再試。");
				return "redirect:/backend/cs/index";
			}
		}
	
}
