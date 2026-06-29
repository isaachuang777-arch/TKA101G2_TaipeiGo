package com.taipeigo.backend.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.taipeigo.admin.model.AdminVO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class BackendLoginFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
// Filter的話用這2句 要轉型 but OncePerRequestFilter是Spring 就不用了
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpServletResponse res = (HttpServletResponse) response;
		
		
		String requestURI = request.getRequestURI();
// ===================================
// 開發用/測試 不用強登 || 要測試請把以line29-32注解=>PS.上線前要注解
// ===================================
		if (true) {
			filterChain.doFilter(request, response);
			return;
		}
		
		
//===================
// login logout 不用強登
//===================
  if (requestURI.contains("/backend/auth/")) {
        //這句等於 檢查完畢 放行~
    filterChain.doFilter(request, response);
    return;
  }
//===================
// /backend/*都強制登入
//===================
  HttpSession session = request.getSession();
  AdminVO adminVO = (AdminVO) session.getAttribute("adminVO");
  
  //沒登入 respone.sendRedirect回登入頁
  if(adminVO == null) {
    //先把這頁記下來 方便登入後直接回去本來的頁面
      session.setAttribute("reUrl", requestURI);
      
    // 沒登入，踢回登入頁
    response.sendRedirect(request.getContextPath() + "/backend/auth/login");
    return;
  }

// ===================================
// 登入強制重設密碼:如果狀態是 9，只能重設密碼或登出
// ===================================
if (adminVO.getAdmStatus() == AdminVO.StatusForcetoChangePW) { 
    // 檢查他是不是想要別的網址
    if (!requestURI.contains("/profile/resetPw") && !requestURI.contains("/auth/logout")) {
        //把他抓回修改密碼的專屬頁面
    response.sendRedirect(request.getContextPath() + "/backend/admin/profile/resetPw");
    return;
    }
}


//===================
// SuperAdmin什麼都能看 funcId =21 = SuperAdmin = 老闆
//===================

  //如果isSuperAdmin = adminVO裡的getAdmPerVO()是查權限紀錄[是set] 放上 stream輸送帶
  //輸送帶上其中一個符合anyMatch(p 是前面那個set<AdmPerVO> -> 檢查p的getAdmFuncVO().getFuncId()是21);
  //adminVO.getAdmPerVO().getAdmFuncVO().getFuncId()
  boolean isSuperAdmin = adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==21);
  if(isSuperAdmin) {

      filterChain.doFilter(request, response);
      return;
  }
//===================
// IT funcId = 24 = 只能看IT能看的東西
//===================    
  if (requestURI.contains("/backend/admin/it")) { 
  boolean isIT= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==24);
  if(!isIT) {
      response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
      return;
  }
}  
//===================
//訂單部 funcId = 22 = 只能看訂能看的東西
//===================    
 if (requestURI.contains("/backend/order")) { 
 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
 if(!isOrder) {
     response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
     return;
 }
}  
 if (requestURI.contains("/backend/ticket")) { 
 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
 if(!isOrder) {
     response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
     return;
 }
}  
 if (requestURI.contains("/backend/ticketCategory")) { 
	 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
	 if(!isOrder) {
		 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
		 return;
	 }
 }  
 if (requestURI.contains("/backend/activity")) { 
	 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
	 if(!isOrder) {
		 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
		 return;
	 }
 }  
 if (requestURI.contains("/backend/ticket/serialList")) { 
	 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
	 if(!isOrder) {
		 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
		 return;
	 }
 }  
 if (requestURI.contains("/backend/product")) { 
	 boolean isOrder= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==22);
	 if(!isOrder) {
		 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
		 return;
	 }
 } 
//===================
//客服中心 funcId = 23 = 只能看訂能看的東西
//===================    
	 if (requestURI.contains("/backend/cs")) { 
		 boolean isCs= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==232);
		 if(!isCs) {
			 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
			 return;
		 }
	 }   
	 if (requestURI.contains("/backend/customer")) { 
		 boolean isCs= adminVO.getAdmPerVO().stream().anyMatch(p -> p.getAdmfuncVO().getFuncId() ==232);
		 if(!isCs) {
			 response.sendRedirect(request.getContextPath() + "/backend/dashboard/index?error=forbidden");
			 return;
		 }
	 }   
  //通過上面的boolean值就能進入
  filterChain.doFilter(request, response);
	}

	 

}