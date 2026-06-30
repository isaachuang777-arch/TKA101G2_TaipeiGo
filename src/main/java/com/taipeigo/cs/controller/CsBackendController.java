package com.taipeigo.cs.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.cs.model.CsMsgRepository;
import com.taipeigo.cs.model.CsMsgVO;
import com.taipeigo.cs.model.CsRepository;
import com.taipeigo.cs.model.CsService;
import com.taipeigo.cs.model.CsVO;

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
	@Value("${taipeigo.upload.base-dir}")
	private String uploadBaseDir;
	
//後台客服首頁+dashboard
	@GetMapping({"/", "/index", "/dashboard"})
	public String showCsdahsboardpage(Model model, @RequestParam(value = "page", defaultValue = "1")  Integer page) {
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
		
		//urgentCases =>只會列5張 其餘會叫html 轉去大表
		Page<CsVO> pageResult = csService.getUrgentCsforindex( yesterday, page);
		model.addAttribute("pageResult", pageResult);
		model.addAttribute("urgentCases", pageResult.getContent());
		
		model.addAttribute("activePage", "cscenter");
		return "backend/cs/index";
	}
	//listAll萬用頁
	@GetMapping("/listAll")
	public String listCase(Model model,
							@RequestParam (value = "caseStatus", required = false) Byte caseStatus,
							@RequestParam (value = "caseCate", required = false) Byte caseCate,
							@RequestParam (value = "keyword", required = false) String keyword,
							@RequestParam(value = "urgent", required = false) Boolean isUrgent,
							@RequestParam(value = "unclosed", required = false) Boolean isUnclosed,
							@RequestParam(value = "page", defaultValue = "1")  Integer page
							) {
		
		Page<CsVO> pageResult = null;
		
		if(Boolean.TRUE.equals(isUrgent)){
			long oneDayAgoMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        	java.sql.Timestamp yesterday = new java.sql.Timestamp(oneDayAgoMillis);
        	pageResult = csService.getUrgentCs(yesterday, page);
		}else if(Boolean.TRUE.equals(isUnclosed)){
			pageResult = csService. getActiveCs(page);
		}else if(keyword != null){
			pageResult  = csService.getCsByCustContainingByPage(keyword, page);
		}else if(caseStatus != null) {
			pageResult = csService.getCsByCaseStatus(caseStatus, page);
		}else if (caseCate != null) {
			pageResult = csService.getCsByCaseCate(caseCate, page);
		}else {
			pageResult = csService.getAllCsByPage(page);
		}
	    
		//保留原來的搜尋條件
	    model.addAttribute("caseStatus", caseStatus);
	    model.addAttribute("caseCate", caseCate);
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("urgent", isUrgent);
	    model.addAttribute("unclosed", isUnclosed);		
		//
        model.addAttribute("pageResult", pageResult);
		model.addAttribute("csList", pageResult.getContent());

		model.addAttribute("activePage", "cscenter");
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

		model.addAttribute("activePage", "cscenter");
		return "backend/cs/view";
	
	}
	//管理員回覆或內部訊息(Woknote)
		@PostMapping("/backendReply")
		public String backendCsReply(HttpSession session,
								@RequestParam("csId") Integer csId,
								@RequestParam("msg") String msg,
								@RequestParam (value = "uploadImg", required = false) MultipartFile uploadImg, 
								@RequestParam ("senderType") Byte senderType,
								RedirectAttributes redirectAttributes)
		{
			//驗證字數
		    if (msg == null || msg.trim().isEmpty() || msg.length() > 500) {
		        redirectAttributes.addFlashAttribute("errorMsg", "回覆內容不能為空，或在 500 字以內！");
		        redirectAttributes.addFlashAttribute("msg", msg);
		        return "redirect:/backend/cs/view?csId=" + csId;
		    }
			//1.由網頁拿資料
			//who reply
			AdminVO adminVO = (AdminVO) session.getAttribute("adminVO");
			//2.開始做打包前置作業 建一個新的Msg
			CsMsgVO csmsgVO = new CsMsgVO();
			//3.打包setter
			//存留言+who
			csmsgVO.setMsgContent(msg);
			csmsgVO.setAdminVO(adminVO);
			//4. 上傳圖片
			String msgImgsrc = null; // 預設沒有圖片
		    if (uploadImg != null && !uploadImg.isEmpty()) {
				try {
					// 取得原本的副檔名 (例如 .jpg, .png)
					String originalFilename = uploadImg.getOriginalFilename();
					String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
					
					// 為了避免檔名重複被覆蓋，我們用 UUID 產生亂碼作為新檔名
					String newFileName = UUID.randomUUID().toString() + extension;
					
					// 建立我們要存檔的實體資料夾路徑 (C:/taipeiGo_uploads/images/cs)
					Path csDirPath = Paths.get(uploadBaseDir, "cs");
					if (!Files.exists(csDirPath)) {
						Files.createDirectories(csDirPath); // 如果資料夾不存在就自動建立
					}
					
					// 把檔案存進去
					Path filePath = csDirPath.resolve(newFileName);
					uploadImg.transferTo(filePath);
					
					// 準備要存進資料庫的相對路徑 (對應到 WebMvcConfig 裡的網址)
					msgImgsrc = "/images/cs/" + newFileName;
					csmsgVO.setMsgImgsrc(msgImgsrc);					
				} catch (IOException e) {
					e.printStackTrace();
					redirectAttributes.addFlashAttribute("errorMsg", "圖片上傳失敗，請稍後再試！");
					return "redirect:/backend/cs/view?csId=" + csId;
				}
		    }
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
