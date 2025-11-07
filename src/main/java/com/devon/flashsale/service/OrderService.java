package com.devon.flashsale.service;

import java.util.List;

import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.enums.OrderStatus;

public interface OrderService {
	
	public List<Order> getAllOrders();

	public Order createOrder(Long eventId, int quantity, String idempotencyKey);
	
	public Order createOrderWithoutOptimisticLock(Long eventId, int quantity, String idempotencyKey);
	
	public Order confirmOrder(PaymentRequestDto paymentRequest);
	
	public Order cancelOrder(Long orderId);
	
	public Order cancelOrderWithoutOptimisticLock(Long orderId);

	public Order getOrderById(Long id);
	
	public void updatePendingOrder(Order order, OrderStatus status);
	
	public List<Order> getAllOrdersByStatus(OrderStatus status);
}
