package com.taipeigo.admin.controller;

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
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AdmFuncService adminFuncService;


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
            redirectAttributes.addFlashAttribute("successMsg", "✅ 已經成功修改管理員 " + adminVO.getAdmName() + " 資料！");

        }catch(RuntimeException e){
                        redirectAttributes.addFlashAttribute("successMsg", "❌ " + e.getMessage());
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

///////////////////User dashboard///////////////////////
//轉去首頁+沒登入時踢回login
@GetMapping("/profile/index")
  public String showDashboard(HttpSession session) {
      if (session.getAttribute("adminVO") == null) {
          // 如果是空值，代表沒登入過，直接把它踢回登入頁面
          return "redirect:/backend/auth/login";
      }
      
      // 如果有登入，才放行讓他看 index.html
      return "backend/admin/profile/index";

  }

}
