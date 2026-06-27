package com.taipeigo.favorite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.favorite.model.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // 我的最愛頁面
    @GetMapping
    public String showFavoritePage(HttpSession session) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        Integer custId = loginCustomer.getCustId();

        // 之後這裡會把會員的我的最愛資料放進 model
        // favoriteService.getFavoritesByCustId(custId);

        return "frontend/favorite/favorite";
    }

    // 加入 / 取消 我的最愛
    @PostMapping("/toggle")
    public String toggleFavorite(
            @RequestParam String type,
            @RequestParam Integer id,
            HttpSession session,
            HttpServletRequest request) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        Integer custId = loginCustomer.getCustId();

        favoriteService.toggleFavorite(custId, type, id);

        String referer = request.getHeader("Referer");

        if (referer != null) {
            return "redirect:" + referer;
        }

        return "redirect:/";
    }
}