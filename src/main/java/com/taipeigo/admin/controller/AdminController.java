package com.taipeigo.admin.controller;

import com.taipeigo.IndexController;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.admin.model.AdmFuncVO;
import com.taipeigo.admin.model.AdminRepository;
import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.admin.model.AdmFuncService;
import com.taipeigo.admin.model.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/backend/admin")


public class AdminController {
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AdmFuncService adminFuncService;
    
    @Autowired
    private AdminRepository adminRepository;
   

    AdminController(IndexController indexController) {
    }


    
//列出全員 + 分頁 + 模糊搜尋
@GetMapping("/it/listAll")
    public String listAlladmin(Model model, HttpSession session, @RequestParam(value = "page", defaultValue = "1")  Integer page,@RequestParam(value = "keyword", required = false) String keyword ) {

        Page<AdminVO> pageResult = null;
        
        //邏輯
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 找到人
                model.addAttribute("keyword", keyword);
                pageResult = adminService.getByAdmAccContainingOrAdmNameContainingBypage(keyword, page);
            } else {//no
                pageResult = adminService.getAllAdminBypage(page);
            }
        } catch (RuntimeException e) {
            // 0resultmsg
            model.addAttribute("errorMsg", e.getMessage());
                pageResult = adminService.getAllAdminBypage(page);
        }

        //丟回前端
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("adminVOList", pageResult.getContent());
        return "backend/admin/it/listAllAdmin";
    }


//(update1)更新會員資料前置作業
@GetMapping("/it/updateAdmin")
    public String updateAdmin(Model model, HttpSession session, @RequestParam("admId") Integer admId){
        //防呆好煩
        if(session.getAttribute("adminVO") == null) {
            return "redirect:/backend/auth/login";
        }
        AdminVO adminVO = adminService.findByAdmId(admId);
        //List<AdmFuncVO>funcList = adminFuncService.getAlladmFuncs();

        model.addAttribute("adminVO", adminVO);
        //model.addAttribute("funcList", funcList);
        return "backend/admin/it/updateAdmin";

    }
//(Update2)(save) admin to DB
@PostMapping("/it/updateAdmintoDB")
    public String updateAdmintoDB(AdminVO adminVO, Model model, HttpSession session, RedirectAttributes redirectAttributes){

    if (session.getAttribute("adminVO") == null){
        return "redirect:/backend/auth/login";
        }

        try{
            adminService.updateAdmin(adminVO);
            redirectAttributes.addFlashAttribute("successMsg", "已經成功修改管理員 " + adminVO.getAdmName() + " 資料！");

        }catch(RuntimeException e){
                        redirectAttributes.addFlashAttribute("errorMsg",  e.getMessage());
        }
        return "redirect:/backend/admin/it/listAll";
    }

//(New1)新增admin頁面時要列出的權限列表
@GetMapping("/it/addAdmin")
    public String getAlladmFuncs(Model model, HttpSession session){
        if(session.getAttribute("adminVO") == null) {
            return "redirect:/backend/auth/login";
        }
        List<AdmFuncVO>funcList = adminFuncService.getAlladmFuncs();
        model.addAttribute("funcList", funcList);
        return "backend/admin/it/addAdmin";
    }
//(New2)真的新增Admin
@PostMapping("/it/createAdmin")
    public String createAdmin (AdminVO adminVO, Integer[] funcIds, Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            adminService.createAdmin(adminVO, funcIds);
            
            redirectAttributes.addFlashAttribute("successMsg", " 已經成功新增 " + adminVO.getAdmAcc() + " 帳號！");
            
            return "redirect:/backend/admin/it/listAll";
        }catch (RuntimeException e){
            
            model.addAttribute("errorMsg", e.getMessage());
            
            model.addAttribute("funcList", adminFuncService.getAlladmFuncs());
            return "backend/admin/it/addAdmin";
        }
    }

//updateStatus with ReponseBody
@PostMapping("/it/updateadmStatus")
@ResponseBody
    public String updateadmStatus(@RequestParam("admId") Integer admId, @RequestParam("newadmStatus") Byte newadmStatus, HttpSession session ){

    //未登入防呆
        if (session.getAttribute("adminVO") == null){
        return "redirect:/backend/auth/login";
        }

        try {
            adminService.updateadmStatus(admId, newadmStatus);
            return "SUCCESS";
        } catch (RuntimeException e) {
            return "ERROR:" + e.getMessage();
        }
    }

//==========================================
//Admin Profile首頁
//==========================================

@GetMapping({"/profile/index", "/profile", "/profile/"})
  public String showDashboard(Model model, HttpSession session) {
	    AdminVO loginAdmin = (AdminVO) session.getAttribute("adminVO");
	    model.addAttribute("adminVO", loginAdmin);
      return "backend/admin/profile/index";
  }
// ==========================================
// IT 管理中心首頁 (4 格卡片入口)
// ==========================================
@GetMapping({"/it/index","/it/","/it"})
public String showITDashboard() {
        
    return "backend/admin/it/index";
}

// ==========================================
// IT強制更改密碼頁面 + Containing搜查 +分頁
// ==========================================
@GetMapping("/it/forceResetPw")
public String showforceResetPw(Model model , @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "page", defaultValue = "1")  Integer page){

    if(keyword != null && !keyword.trim().isEmpty()){
        try{
            //Containing查詢
            Page<AdminVO> pageResult = adminService.getByAdmAccContainingOrAdmNameContainingBypage(keyword, page);

            model.addAttribute("pageResult", pageResult);
            model.addAttribute("adminVOList", pageResult.getContent());
        }catch(RuntimeException e){
            model.addAttribute("errorMsg", e.getMessage());
        }
    }
    //Keyword留在網頁
    model.addAttribute("keyword", keyword);
    
    return "backend/admin/it/forceResetPw";
}

// ==========================================
// 確定 IT強制更改密碼
// ==========================================
@PostMapping("/it/saveForcePwtoDB")
    public String saveForcePwtoDB(@RequestParam("admId") Integer admId, RedirectAttributes redirectAttributes,AdminVO adminVO){
        try {
            // 將表格的 admId 丟給itforceResetPw處理
            adminService.itforceResetPw(admId);
            
            AdminVO adminVO1 = adminService.findByAdmId(admId);
            redirectAttributes.addFlashAttribute("successMsg", "密碼已成功強制重設為預設值！" + adminVO1.getAdmAcc()  + " 登入時將被強制重設密碼。");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "重設失敗：" + e.getMessage());
        }

    return "redirect:/backend/admin/it/listAll";

}
// ==========================================
// 掛管理員 重設密碼頁面
// ==========================================
@GetMapping("/profile/resetPw")
    public String showForceChangePwPage() {
        return "backend/admin/profile/resetPw"; 
    }
// ==========================================
// 管理員重設密碼
// ==========================================
@PostMapping("/profile/resetPw")
    public String adminResetPw (
        @RequestParam("oldPw") String oldPw,
        @RequestParam("newPw") String newPw,
        @RequestParam("confirmPw") String confirmPW,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ){
        AdminVO adminVO = (AdminVO) session.getAttribute("adminVO");

        //新密碼防呆
        if(!newPw.equals(confirmPW)){
            redirectAttributes.addFlashAttribute("errorMsg", "兩次輸入的新密碼不一致！請重新輸入。");
            return "redirect:/backend/admin/profile/resetPw";
        }
        try {
                // Service ->驗證密碼+改密碼+轉Status
                adminService.adminResetPw(adminVO.getAdmId(), oldPw, newPw);
                
                //確保session裡的adminVO是最新的
                adminVO.setAdmPw(newPw);
                adminVO.setAdmStatus(AdminVO.StatusEnabled);
                session.setAttribute("adminVO", adminVO);
                
                //重設完成
                redirectAttributes.addFlashAttribute("successMsg", "密碼修改成功！");
                return "redirect:/backend/dashboard/index"; 
                
            } catch (RuntimeException e) {
                // 舊密碼打錯，或是其他錯誤
                redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
                return "redirect:/backend/admin/profile/resetPw";
            }
	}
// ==========================================
// 權限中心: 
// ==========================================
@GetMapping("/it/permission")
	public String  showPermission(
	    @RequestParam(required = false) Integer funcId,
	    Model model,
        @RequestParam(value = "page", defaultValue = "1")  Integer page
	){
	    Page<AdminVO> pageResult;
	    //5. 新增無權限判斷 (funcId==0) =>顯示無權限者
	    if(funcId !=null && funcId==0) {
	    pageResult = adminService.getByAdmPerVOisEmptyByPage(page);
		}else if(funcId != null){ //1. 先判斷是要全list還是以by權限 =>顯示Admin的List
	       pageResult = adminService.getByAdminByFuncIdByPage(funcId, page);
	    }else{ //沒輸入就代表給他全表
	       pageResult = adminService.getAllAdminBypage(page);
	    }
	    //2. 顯示各權限 => FuncName
	    List<AdmFuncVO> admFuncList = adminFuncService.getAlladmFuncs();
	    
	    //3. 送去前端
	    model.addAttribute("admFuncList", admFuncList);
	    model.addAttribute("pageResult", pageResult);
	    model.addAttribute("adminList", pageResult.getContent());
	    
	    
	    //4.前端有選哪個權限 就回傳是哪個funcId
	    model.addAttribute("selectedfuncId", funcId);	
	    
	    //6.回傳無權限人數
	    long noPeradmin = adminRepository.countByAdmPerVOsIsEmpty();
	    model.addAttribute("noPeradmin", noPeradmin);
	    
	    return "backend/admin/it/permission";
}
//==========================================
//更改權限1: 
//==========================================
@GetMapping("/it/updatepermission")
public String Showupdatepermission(Model model, @RequestParam("admId") Integer admId) {
    AdminVO adminVO = adminService.findByAdmId(admId);
    List<AdmFuncVO>funcList = adminFuncService.getAlladmFuncs();

    model.addAttribute("adminVO", adminVO);
    model.addAttribute("funcList", funcList);
	
	return "backend/admin/it/updatepermission";
}
//==========================================
//更改權限2: 
//==========================================
@PostMapping("/it/updatepermission")
public String updatepermission(@RequestParam("admId") Integer admId, Integer[] funcIds, Model model,RedirectAttributes redirectAttributes) {
	adminService.updateAdminPer(admId, funcIds);	
		AdminVO adminVO = adminService.findByAdmId(admId);	
	redirectAttributes.addFlashAttribute("successMsg", "成功修改 " + adminVO.getAdmName() + " 的權限！");
	return "redirect:/backend/admin/it/permission";
}
}
