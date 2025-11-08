package com.devon.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The point of the Flash Sale Ticketing SpringBoot Application
 */
@SpringBootApplication
@EnableScheduling
public class FlashsaleTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashsaleTicketingApplication.class, args);
	}

}
