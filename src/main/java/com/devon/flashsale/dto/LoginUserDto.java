package com.devon.flashsale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginUserDto {

	@NotBlank(message = "Please enter your email")
    @Email(message = "Please provide a valid email address")
	private String email;
	
	@NotBlank(message = "Please enter your password")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
