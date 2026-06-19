package com.taipeigo.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.admin.model.AdminService;
import com.taipeigo.admin.model.AdminVO;

import jakarta.servlet.http.HttpSession;

////////////////////////////////////////
/// This controller
//no need filter
///  the adminuser login or logout
///  :)
////////////////////////////////////////

@Controller
@RequestMapping("/backend")
public class BackendAuthController {

    @Autowired
    private AdminService adminService;

//只是顯示login page
@GetMapping("/auth/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("adminVO") !=null) {
            // 如果session已經有VO 還跑login就跑回後台首頁
            return "redirect:/backend/dashboard/index";
        }
        return "backend/auth/login"; // 導向 templates 裡的 login.html
    }

// 登入
@PostMapping("/auth/login")
	public String adminLogin(@RequestParam("admAcc") String admAcc,
							@RequestParam("admPw") String admPw,
							Model model, HttpSession session) {
		try {
            //Service的adminLogin
            AdminVO adminVO =adminService.adminLogin(admAcc, admPw);
            //有找到 可以登入
            if(adminVO != null) {
                session.setAttribute("adminVO", adminVO);
                
                //有了Filter後新增:強登後可以回去剛才被踢掉的頁面[reUrl]
                //先去Session找有沒有reUrl 如果有 就先redirect去剛被踢的
                String reUrl = (String) session.getAttribute("reUrl");
                if(reUrl !=null) {
                	//先移除session裡面的redirectUrl
                	session.removeAttribute("reUrl");
                	//再redirect String裡面存的redirectUrl
                	return "redirect:"+ reUrl;
                }
                
                return "redirect:/backend/dashboard/index";
            }
		}catch(RuntimeException e){
			model.addAttribute("errorMsg", e.getMessage());
		}
				
		return "backend/auth/login";
	}

//登出
@PostMapping("/auth/logout")
    public String logout (HttpSession session) {
        
        session.invalidate();
            return "redirect:/backend/auth/login";
    }
	
}
