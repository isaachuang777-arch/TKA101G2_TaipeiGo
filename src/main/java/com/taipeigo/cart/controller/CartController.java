package com.taipeigo.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class CartController {

	@Autowired(required = false)
	private RedisTemplate<String, Object> redisTemplate;

	@GetMapping("/test")
	public String test() {

		if (redisTemplate == null) {
			return "Redis not available";
		}

		redisTemplate.opsForValue().set("hello", "TaipeiGO");

		return (String) redisTemplate.opsForValue().get("hello");
	}
}
