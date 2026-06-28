package com.taipeigo.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
	
	@GetMapping("/me")
	public ResponseEntity<?> getLoginCustomer(HttpSession session) {

	    CustomerVO loginCustomer =
	            (CustomerVO) session.getAttribute("loginCustomer");

	    if (loginCustomer == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of(
	                        "login", false,
	                        "message", "尚未登入"
	                ));
	    }
	    
	    // 回傳給前端知道目前是否已登入，以及登入的custID
	    return ResponseEntity.ok(Map.of(
	    			"login", true,
	            "custId", loginCustomer.getCustId()
	    ));
	}
	
}
