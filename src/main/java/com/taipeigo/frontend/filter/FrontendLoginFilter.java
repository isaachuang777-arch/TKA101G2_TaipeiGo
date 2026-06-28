package com.taipeigo.frontend.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class FrontendLoginFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // ===================
        // 開發用：如果想暫時關閉強制登入，把 false 改 true
        // ===================
        if (false) {
            filterChain.doFilter(request, response);
            return;
        }

        // 前台登入、註冊、驗證信、登入狀態查詢 API 不需強制登入
        if (requestURI.contains("/auth/login")
                || requestURI.contains("/auth/register")
                || requestURI.contains("/auth/verify")
                || requestURI.contains("/api/auth/me")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ===================
        // 檢查前台會員登入狀態
        // ===================
        HttpSession session = request.getSession(false);

        CustomerVO loginCustomer = null;

        if (session != null) {
            loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");
        }

        // 沒登入 → 回登入頁
        if (loginCustomer == null) {

            // 記住原本想去的頁面，之後登入成功可以導回
            HttpSession newSession = request.getSession();

            String referer = request.getHeader("Referer");
            
            if (requestURI.startsWith("/favorite/toggle/ajax") && referer != null) {

                newSession.setAttribute("frontendReUrl", referer);

            } else {

                String queryString = request.getQueryString();

                String fullUrl = requestURI + (queryString != null ? "?" + queryString : "");

                newSession.setAttribute("frontendReUrl", fullUrl);
            }

            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 已登入 → 放行
        filterChain.doFilter(request, response);
    }
}