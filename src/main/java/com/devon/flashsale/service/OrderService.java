package com.devon.flashsale.service;

import java.util.List;

import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.enums.OrderStatus;

public interface OrderService {
	
	/**
	 * Fetches and returns all orders.
	 */
	public List<Order> getAllOrders();
	
	/**
	 * Fetch order for a particular orderId.
	 * @param id : The orderId
	 * @return The fetched Order
	 */
	public Order getOrderById(Long id);
	
	/**
	 * Fetches all the orders having a particular status.
	 * @param status : The order status
	 */
	public List<Order> getAllOrdersByStatus(OrderStatus status);
	
	/** 
	 * Creates a new Order for the event with a particular eventId.
	 * Updates the remaining seats left for the event.
	 * @param eventId : The eventId of the Event
	 * @param quantity : The number of seats required
	 * @param idempotencyKey : A unique identifier associated with each order
	 * @return The Order created
	 */
	public Order createOrder(Long eventId, int quantity, String idempotencyKey);
	
	/** 
	 * Creates a new Order for the event with a particular eventId.
	 * Updates the remaining seats left for the event.
	 * This method avoids Optimistic Lock and relies on Atomic DB update for concurrency.
	 * @param eventId : The eventId of the Event
	 * @param quantity : The number of seats required
	 * @param idempotencyKey : A unique identifier associated with each order
	 * @return The Order created
	 */
	public Order createOrderWithoutOptimisticLock(Long eventId, int quantity, String idempotencyKey);
	
	
	/**
	 * Confirm an Pending Order.
	 * @param paymentRequest : DTO object containing order and payment details
	 * @return The confirmed Order
	 */
	public Order confirmOrder(PaymentRequestDto paymentRequest);
	
	
	/** 
	 * Cancel an existing order.
	 * Updates the seats associated of the event associated with the order.
	 * @param orderId : The orderId of the order
	 * @return The cancelled order
	 */
	public Order cancelOrder(Long orderId);
	
	/** 
	 * Cancel an existing order.
	 * Updates the seats associated of the event associated with the order.
	 * This method avoids Optimistic Lock and relies on Atomic DB update for concurrency.
	 * @param orderId : The orderId of the order
	 * @return The cancelled order
	 */
	public Order cancelOrderWithoutOptimisticLock(Long orderId);
	
	/**
	 * Expires an Order which is in PENDING state for more than 5 minutes.
	 * Updates the seats associated of the event associated with the order.
	 * @param order : The order to expire
	 */
	public void expirePendingOrder(Order order);
	
	/**
	 * Expires an Order which is in PENDING state for more than 5 minutes.
	 * Updates the seats associated of the event associated with the order.
	 * This method avoids Optimistic Lock and relies on Atomic DB update for concurrency.
	 * @param order : The order to expire
	 */
	public void expirePendingOrderWithoutOptimisticLock(Order order);
}
