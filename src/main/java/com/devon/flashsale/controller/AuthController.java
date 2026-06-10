package com.devon.flashsale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.dto.LoginUserDto;
import com.devon.flashsale.dto.RegisterUserDto;
import com.devon.flashsale.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthenticationManager authenticationManager;
	public final UserService userService;
	
	
	public AuthController(AuthenticationManager authenticationManager, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<String> registerNewUser(@RequestBody @Valid RegisterUserDto newUserDetails) {
		log.info("Creating new User with name "+newUserDetails.getName());
		userService.createNewUser(newUserDetails);
		return ResponseEntity.ok("Registration successful");
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> loginNewUser(@RequestBody @Valid LoginUserDto loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
																new UsernamePasswordAuthenticationToken(
																		loginRequest.getEmail(), 
																		loginRequest.getPassword()
																)
															);
		return ResponseEntity.ok("Login successful");
	}	
}
