package com.devon.flashsale.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {
	
	private int status;
	
	private String timestamp;
	
	private String errorMessage;
	
	public ErrorResponseDto(int status, String errorMessage) {
		this.status = status;
		this.timestamp = LocalDateTime.now().toString();
		this.errorMessage = errorMessage;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
