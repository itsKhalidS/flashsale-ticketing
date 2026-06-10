package com.devon.flashsale.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for sending error responses to the caller
 */
public class ErrorResponseDto {
	
	private int status;
	
	private String timestamp;
	
	private List<String> exceptions;
	
	public ErrorResponseDto(int status, String errorMessage) {
		this.status = status;
		this.timestamp = LocalDateTime.now().toString();
		this.exceptions = new ArrayList<>();
		exceptions.add(errorMessage);
	}
	
	public ErrorResponseDto(int status, List<String> exceptions) {
		this.status = status;
		this.timestamp = LocalDateTime.now().toString();
		this.exceptions = exceptions;
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

	public List<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}

}
