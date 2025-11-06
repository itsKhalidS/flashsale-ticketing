package com.devon.flashsale.service;

import java.util.List;

import com.devon.flashsale.entity.Order;

public interface OrderService {
	
	public List<Order> fetchAllOrders();

	public Order createOrder(Long eventId, int quantity, String idempotencyKey);
	
	public Order confirmOrder(Long OrderId);
	
	public Order cancelOrder(Long orderId);

	public Order fetchOrderById(Long id);
}
