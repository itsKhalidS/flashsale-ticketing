package com.devon.flashsale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.dto.LoginResponseDto;
import com.devon.flashsale.dto.LoginUserDto;
import com.devon.flashsale.dto.RegisterUserDto;
import com.devon.flashsale.entity.User;
import com.devon.flashsale.service.UserService;
import com.devon.flashsale.service.security.impl.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	public final UserService userService;
	public final JwtService jwtService;
	
	public AuthController(UserService userService, JwtService jwtService) {
		this.userService = userService;
		this.jwtService = jwtService;
	}
	
	@PostMapping("/register")
	@ResponseBody
	public ResponseEntity<String> registerNewUser(@RequestBody @Valid RegisterUserDto newUserDetails) {
		log.info("Creating new User with name "+newUserDetails.getName());
		userService.createNewUser(newUserDetails);
		return ResponseEntity.ok("Registration successful");
	}
	
	@PostMapping("/login")
	@ResponseBody
	public  ResponseEntity<LoginResponseDto> loginNewUser(@RequestBody @Valid LoginUserDto loginRequest) {
		User user = userService.loginUser(loginRequest);
		return ResponseEntity.ok(new LoginResponseDto(jwtService.generateJwtToken(user)));
	}	
}
