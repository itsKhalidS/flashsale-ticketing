package com.devon.flashsale.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.dto.UserProfileDto;
import com.devon.flashsale.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/profile")
	@ResponseBody
	public UserProfileDto getMyDetails() {
		return userService.getCurrentUserProfile();
	}

}
