package com.devon.flashsale.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO class containing payment details. Used to map incoming <code>/order/{orderId}/confirm</code> requests
 */
public class PaymentRequestDto {

	private Long orderId;
	
    @Size(max=100, message = "Payment Reference length exceeded")
    private String paymentReference;
    
    private Double amount;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}
