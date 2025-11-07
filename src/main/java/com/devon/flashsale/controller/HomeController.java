package com.devon.flashsale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {
	
	private static final Logger log = LoggerFactory.getLogger(HomeController.class);
	
	@GetMapping
	public String home() {
		log.info("Welcome to the Backend of the Flash Sale Ticketing App");
		return "Welcome to the Backend of the Flash Sale Ticketing App";
	}

}
