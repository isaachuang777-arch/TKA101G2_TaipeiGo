package com.taipeigo.favorite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.favorite.model.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.ui.Model;
import com.taipeigo.favorite.dto.FavoriteDTO;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // 我的最愛頁面
    @GetMapping
    public String showFavoritePage(HttpSession session, Model model) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        Integer custId = loginCustomer.getCustId();

        List<FavoriteDTO> favoriteList = favoriteService.getFavoriteDTOByCustId(custId);

        model.addAttribute("favoriteList", favoriteList);

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
    
    // AJAX：加入 / 取消 我的最愛
    @PostMapping("/toggle/ajax")
    @ResponseBody
    public boolean toggleFavoriteAjax(
            @RequestParam String type,
            @RequestParam Integer id,
            HttpSession session) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return false;
        }

        Integer custId = loginCustomer.getCustId();

        return favoriteService.toggleFavorite(custId, type, id);
    }
    
    // AJAX：檢查是否已加入我的最愛
    @GetMapping("/check")
    @ResponseBody
    public boolean checkFavorite(
            @RequestParam String type,
            @RequestParam Integer id,
            HttpSession session) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return false;
        }

        Integer custId = loginCustomer.getCustId();

        return favoriteService.isFavoriteByTypeAndId(custId, type, id);
    }
}