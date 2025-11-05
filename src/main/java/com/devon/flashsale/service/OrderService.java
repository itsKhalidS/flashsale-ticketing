package com.devon.flashsale.service;

import com.devon.flashsale.entity.Order;

public interface OrderService {

	public Order createNewOrder(Long eventId, int quantity, String idempotencyKey);
	
	public Order confirmOrder(Long OrderId);
	
	public Order cancelOrder(Long orderId);
}
