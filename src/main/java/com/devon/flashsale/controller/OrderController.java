package com.devon.flashsale.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.dto.OrderRequestDto;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.service.OrderService;
import com.devon.flashsale.validation.OrderValidator;

@RestController
@RequestMapping("/order")
public class OrderController {

	private final OrderService orderService;
	
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@GetMapping
	@ResponseBody
	public List<Order> fetchAllOrders(){
		return orderService.fetchAllOrders();
	}
	
	@GetMapping("/{id}")
	@ResponseBody
    public Order getOrderById(@PathVariable Long id) {
        return orderService.fetchOrderById(id);
    }

	@PostMapping("/create")
	@ResponseBody
	public Order createNewOrder(@RequestBody OrderRequestDto orderRequest) {
		List<FlashSaleAppException> exceptions = OrderValidator.validateNewOrder(orderRequest.getEventId());
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		return orderService.createOrder(orderRequest.getEventId(), orderRequest.getQuantity(), orderRequest.getIdempotencyKey());
	}
	
	@PutMapping("/{orderId}/confirm")
	@ResponseBody
    public Order confirmOrder(@PathVariable Long orderId) {
		//Payment Logic
        return orderService.confirmOrder(orderId);
        
    }
	
	@PutMapping("/{orderId}/cancel")
	@ResponseBody
    public Order cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }
}
