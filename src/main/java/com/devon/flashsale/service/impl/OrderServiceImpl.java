package com.devon.flashsale.service.impl;

import com.devon.flashsale.entity.Order;
import com.devon.flashsale.service.OrderService;

public class OrderServiceImpl implements OrderService {

	@Override
	public Order createNewOrder(Long eventId, int quantity, String idempotencyKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order confirmOrder(Long OrderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order cancelOrder(Long orderId) {
		// TODO Auto-generated method stub
		return null;
	}

}
