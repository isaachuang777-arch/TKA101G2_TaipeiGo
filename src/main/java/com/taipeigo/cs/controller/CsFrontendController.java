package com.taipeigo.cs.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.cs.model.CsMsgVO;
import com.taipeigo.cs.model.CsService;
import com.taipeigo.cs.model.CsVO;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/CustomerService")

public class CsFrontendController {
	
	@Autowired
	private CsService csService;
	
	@Value("${taipeigo.upload.base-dir}")
	private String uploadBaseDir;
	
//掛客服中心首頁
	@GetMapping({"/index","/",""})
	public String showCsIndexPage() {
		return "frontend/cs/index";
	}

//掛新增詢問頁面
	@GetMapping({"/createticket","/createticket/"})
	public String showcreateTicketpage() {
		return "frontend/cs/createticket";
	}
	
//按下新增詢問  CsVO createTicket(Integer custId, Byte caseCate, String msg, String msgImgsrc)
	@PostMapping("/createticket")
	public String createticketToDB(HttpSession session, 
			@RequestParam("caseCate") Byte caseCate,
			@RequestParam("msg") String msg, 
			@RequestParam (value = "uploadImg", required = false) MultipartFile uploadImg, 
			Model model,
			RedirectAttributes redirectAttributes) {

		//1. who create the ticket
		CustomerVO customerVO = (CustomerVO) session.getAttribute("loginCustomer");
		Integer custId= customerVO.getCustId();
		//驗證字數
	    if (msg == null || msg.trim().isEmpty() || msg.length() > 500) {
	        redirectAttributes.addFlashAttribute("errorMsg", "回覆內容不能為空，或在 500 字以內！");
	        redirectAttributes.addFlashAttribute("caseCate", caseCate);
	        redirectAttributes.addFlashAttribute("msg", msg);
	        
	        return "redirect:/CustomerService/createticket";
	    }		
		
		//2. 上傳圖片
		String msgImgsrc = null;
		if (uploadImg != null && !uploadImg.isEmpty()) {
			try {
				String originalFilename = uploadImg.getOriginalFilename();
				String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
				String newFileName = UUID.randomUUID().toString() + extension;
				
				Path csDirPath = Path.of(uploadBaseDir, "cs");
				if (!Files.exists(csDirPath)) {
					Files.createDirectories(csDirPath);
				}
				
				Path filePath = csDirPath.resolve(newFileName);
				uploadImg.transferTo(filePath);
				
				msgImgsrc = "/images/cs/" + newFileName;
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute("errorMsg", "圖片上傳失敗，請稍後再試！");
				return "redirect:/CustomerService/createticket";
			}
		}

		//3.
		try {
			//把newticket丟給service
			CsVO newTicket = csService.createTicket(custId, caseCate, msg, msgImgsrc);
			//csId = 用newticket 取新的CsId
			Integer csId = newTicket.getCsId();
			//新增後就可以回傳到看到自己留言的頁面
			redirectAttributes.addFlashAttribute("successMsg", "謝謝您的詢問，已經新增一個問題，請耐心等待客服人員回覆。");	
			return "redirect:/CustomerService/viewmsgs?csId=" + csId;
		}catch(RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMsg",  "系統維護中，請稍後再試。");
		return "redirect:/frontend/cs/index";
		}

	}
	
//ActivecaseList
	@GetMapping("/ActiveCase")
	public String showactivecaseList(HttpSession session, Model model, @RequestParam(value = "page", defaultValue = "1")  Integer page) {
		CustomerVO customerVO = (CustomerVO) session.getAttribute("loginCustomer");  //去session找登入的人
		Integer custId = customerVO.getCustId(); // custId = session找出的VO.再做getCustId()
		Page<CsVO> pageResult = csService.findByActiveCases(custId, page ); 
		model.addAttribute("pageResult", pageResult); //存進model 叫csList
		model.addAttribute("csList", pageResult.getContent()); //存進model 叫csList
		
		return "frontend/cs/activecase";
	}
//InactivecaseList
		@GetMapping("/InactiveCase")
		public String showinactivecaseList(HttpSession session, Model model, @RequestParam(value = "page", defaultValue = "1")  Integer page) {
			CustomerVO customerVO = (CustomerVO) session.getAttribute("loginCustomer");
			Integer custId = customerVO.getCustId();
			Page<CsVO> pageResult = csService.findByInactiveCases(custId,  page );

			model.addAttribute("pageResult", pageResult); //存進model 叫csList
			model.addAttribute("csList", pageResult.getContent()); //存進model 叫csList
			
			return "frontend/cs/inactivecase";
		}	
//找出使用者能看的Msg List<CsMsgVO> custFindMsg(Integer csId)
		@GetMapping("/viewmsgs")
		public String viewMsgs(HttpSession session, Model model, 
				@RequestParam(value="csId",required = false) Integer csId,
				RedirectAttributes redirectAttributes
				) {
			 //查這單子號碼收vo
			 CsVO csVO =csService.findByCsId(csId);
			//防止有人亂打號碼
			 if (csVO == null || csId < 999 || csId==null) {
				 redirectAttributes.addFlashAttribute("errorMsg",  "查無資料。");
		            return "redirect:/CustomerService/index";
		        }

			//防止偷看
			//現在登人的人
			 CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");
			 Integer currentCustId = loginCustomer.getCustId();
			
			 //如果他想偷看
			 if (!csVO.getCustomerVO().getCustId().equals(currentCustId) ){
			 	redirectAttributes.addFlashAttribute("errorMsg",  "查無資料。");
			 	return "redirect:/CustomerService/index";
			 }
			
			CsVO csVO1 = csService.findByCsId(csId);
			
			List<CsMsgVO> csMsgVO = csService.custFindMsg(csId);
			
			model.addAttribute("caseData", csVO1);
			model.addAttribute("csMsg", csMsgVO);
			
			return "frontend/cs/viewmsgs";
		}
		//亂打字防呆
		@ExceptionHandler(MethodArgumentTypeMismatchException.class)
		public String handleTypeMismatch(RedirectAttributes redirectAttributes) {
			redirectAttributes.addFlashAttribute("errorMsg",  "查無資料。");
		    return "redirect:/CustomerService/index"; 
		}

//前台使用者回覆 customerreply(CsMsgVO newMsg, Integer csId)
		@PostMapping("/custReply")
		public String custReply(HttpSession session,
								@RequestParam("csId") Integer csId,
								@RequestParam("msg") String msg,
								@RequestParam (value = "uploadImg", required = false) MultipartFile uploadImg,
								RedirectAttributes redirectAttributes) 
		{
			//驗證字數
		    if (msg == null || msg.trim().isEmpty() || msg.length() > 500) {
		        redirectAttributes.addFlashAttribute("errorMsg", "回覆內容不能為空，或在 500 字以內！");
		        redirectAttributes.addFlashAttribute("msg", msg);
		        return "redirect:/CustomerService/viewmsgs?csId=" + csId;
		    }
			//1.由網頁拿資料
			//who reply
			CustomerVO customerVO = (CustomerVO) session.getAttribute("loginCustomer");
			//2.開始做打包前置作業 建一個新的Msg
			CsMsgVO csmsgVO = new CsMsgVO();
			//3.打包setter
			//存留言+who
			csmsgVO.setMsgContent(msg);
			csmsgVO.setCustomerVO(customerVO);
			
			//上傳圖片
			if (uploadImg != null && !uploadImg.isEmpty()) {
				try {
					String originalFilename = uploadImg.getOriginalFilename();
					String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
					String newFileName = UUID.randomUUID().toString() + extension;
					
					Path csDirPath = Path.of(uploadBaseDir, "cs");
					if (!Files.exists(csDirPath)) {
						Files.createDirectories(csDirPath);
					}
					
					Path filePath = csDirPath.resolve(newFileName);
					uploadImg.transferTo(filePath);
					
					csmsgVO.setMsgImgsrc("/images/cs/" + newFileName);
				} catch (IOException e) {
					e.printStackTrace();
					redirectAttributes.addFlashAttribute("errorMsg", "圖片上傳失敗，請稍後再試！");
					return "redirect:/CustomerService/viewmsgs?csId=" + csId;
				}
			}
			
			try {
			//丟回Service做事
			csService.customerreply(csmsgVO , csId);
			redirectAttributes.addFlashAttribute("successMsg", "謝謝您的留言，已新增一個回覆，請耐心等待客服人員處理。");
			}catch(RuntimeException e) {
				redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
			return "redirect:/CustomerService/index";
			}
			
		return "redirect:/CustomerService/viewmsgs?csId=" + csId;
		}

//前台使用結案 customerclose(Integer csId)
		@PostMapping("/custClosecase")
		public String custClosecase(HttpSession session,
						@RequestParam("csId") Integer csId,
						RedirectAttributes redirectAttributes)
		{
			try{
				csService.customerclose(csId);
				redirectAttributes.addFlashAttribute("successMsg", "謝謝您的反饋。若有其他問題，請建立新的客服查詢。");
				return "redirect:/CustomerService/viewmsgs?csId=" + csId;
			}catch(RuntimeException e) {
				redirectAttributes.addFlashAttribute("errorMsg",  "系統維護中，請稍後再試。");
				return "redirect:/CustomerService/index";
			}
		}
}
