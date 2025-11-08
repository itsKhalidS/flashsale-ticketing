package com.devon.flashsale.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devon.flashsale.entity.Order;
import com.devon.flashsale.enums.OrderStatus;
import com.devon.flashsale.service.OrderService;

@Component
public class OrderScheduler {

	private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);
	
	private final OrderService orderService;
	
	public OrderScheduler(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@Scheduled(fixedDelay = 60000)
	public void run() {
		log.info("Executing scheduled Order Status updation");
		List<Order> orders = orderService.getAllOrdersByStatus(OrderStatus.PENDING);
		for(Order order: orders) {
			LocalDateTime currentDateTime = LocalDateTime.now();
			if(currentDateTime.isAfter(order.getExpiresAt())) {
				orderService.expirePendingOrder(order);
			}
		}
		log.info("Ending scheduled Order Status updation");
	}
}
