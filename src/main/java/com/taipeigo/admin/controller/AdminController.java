package com.taipeigo.admin.controller;

import com.taipeigo.IndexController;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.admin.model.AdmFuncVO;
import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.admin.model.AdmFuncService;
import com.taipeigo.admin.model.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/backend/admin/")
////////////////////////////////////////
/// This controller for IT admin
/// - listAlluser
/// - update admin permission
/// - update admin status
/// - contatining seach for admin
/// - add new admin
/// ---->Need filter for IT admin only
/// ---->Need force login
////////////////////////////////////////
public class AdminController {
    private final IndexController indexController;

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AdmFuncService adminFuncService;



    AdminController(IndexController indexController) {
        this.indexController = indexController;
    }


    
//列出全員
@GetMapping("/it/listAll")
    public String listAlladmin(Model model, HttpSession session) {
        //防止沒登入的看到
        if(session.getAttribute("adminVO") == null) {
            return "redirect:/backend/auth/login";
        }
        //Listall
        List<AdminVO> adminVOList = adminService.getAllAdmin();
        
        //丟回前端
        model.addAttribute("adminVOList", adminVOList);
        
        
        return "backend/admin/it/listAllAdmin";
    }

//列出Containing List
@GetMapping("/it/listContaining")
    public String listContainingadmin(Model model, HttpSession session , @RequestParam(value = "keyword", required = false) String keyword ) {
        if (session.getAttribute("adminVO") == null) {
            return "redirect:/backend/auth/login";
        }

        List<AdminVO> adminVOList = null;
        
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 找到人
                adminVOList = adminService.findAdminsbyContaining(keyword);
            } else {//no
                adminVOList = adminService.getAllAdmin();
            }
        } catch (RuntimeException e) {
            // 0resultmsg
            model.addAttribute("errorMsg", e.getMessage());
                adminVOList = adminService.getAllAdmin();
        }

        model.addAttribute("adminVOList", adminVOList);
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
        //防呆好煩
        if(session.getAttribute("adminVO") == null) {
            return "redirect:/backend/auth/login";
        }
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
//User Profile首頁
//==========================================
//轉去首頁+沒登入時踢回login
@GetMapping("/profile/index")
  public String showDashboard(HttpSession session) {
//      if (session.getAttribute("adminVO") == null) {
//          // 如果是空值，代表沒登入過，直接把它踢回登入頁面
//          return "redirect:/backend/auth/login";
//      }
      
      // 如果有登入，才放行讓他看 index.html
      return "backend/admin/profile/index";

  }
// ==========================================
// IT 管理中心首頁 (4 格卡片入口)
// ==========================================
@GetMapping({"/it/index","/it/"})
public String showITDashboard() {
        
    return "backend/admin/it/index";
}

// ==========================================
// IT強制更改密碼頁面 + Containing搜查
// ==========================================
@GetMapping("/it/forceResetPw")
public String showforceResetPw(Model model , @RequestParam(value = "keyword", required = false) String keyword){

    if(keyword != null && !keyword.trim().isEmpty()){
        try{
            //Containing查詢
            List<AdminVO> searchResult = adminService.findAdminsbyContaining(keyword);
            model.addAttribute("adminVOList", searchResult);
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
            
            
            redirectAttributes.addFlashAttribute("successMsg", "密碼已成功強制重設為預設值！" + adminVO.getAdmName() + " 登入時將被強制重設密碼。");
            
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

}
