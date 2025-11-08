package com.devon.flashsale.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO class containing order creation details. Used to map incoming <code>/order/create</code> requests
 */
public class OrderRequestDto {

	Long eventId;
	
	int quantity;
	
    @Size(max=100, message = "Idempotency Key length exceeded")
	String idempotencyKey;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}
	
}
