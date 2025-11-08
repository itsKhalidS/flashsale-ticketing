package com.devon.flashsale.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.devon.flashsale.dto.OrderRequestDto;
import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.ValidationException;

/**
 * Helper class to help in Order Validation
 */
public class OrderValidator {
	
	/**
	 * Validates an incoming Order Request
	 * @param orderRequest : The order request object
	 * @return The List of errors
	 */
	public static List<FlashSaleAppException> validateNewOrder(OrderRequestDto orderRequest){
		List<FlashSaleAppException> exceptions = new ArrayList<>();
		
		if(orderRequest.getQuantity() <= 0) {
			exceptions.add(new ValidationException("Order Quantity should be a positive number"));
		}
		
		if(StringUtils.isEmpty(orderRequest.getIdempotencyKey())) {
			exceptions.add(new ValidationException("Invalid idempotency key provided"));
		}
		
		return exceptions;
	}

	/**
	 * Validates an incoming Payment Request
	 * @param paymentRequest : The payment request object
	 * @return The List of errors
	 */
	public static List<FlashSaleAppException> validatePaymentRequest(PaymentRequestDto paymentRequest) {
		List<FlashSaleAppException> exceptions = new ArrayList<>();
		
		if(paymentRequest.getAmount() < 0) {
			exceptions.add(new ValidationException("Amount cannot be negative"));
		}
		
		if(StringUtils.isEmpty(paymentRequest.getPaymentReference())) {
			exceptions.add(new ValidationException("Invalid Payment Reference Number provided"));
		}
		
		return exceptions;
	}
}
