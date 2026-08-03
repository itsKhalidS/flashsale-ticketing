package com.devon.flashsale.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.dto.OrderRequestDto;
import com.devon.flashsale.dto.OrderResponseDto;
import com.devon.flashsale.dto.PageResponse;
import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.service.OrderService;
import com.devon.flashsale.validation.OrderValidator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/order")
public class OrderController {

	private static final Logger log = LoggerFactory.getLogger(OrderController.class);
	
	private static final int pageSize = 12;
	private static final String sortBy = "createdAt";
	private static final String sortDirection = "desc";
	
	private final OrderService orderService;
	
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	/**
	 * @return The List of all Orders
	 */
	@GetMapping
	@ResponseBody
	public PageResponse<OrderResponseDto> fetchAllOrders(@RequestParam(defaultValue = "0") int page){
		log.info("Fetching all orders for page={}, size={}, sortBy={}, direction={}",page, pageSize, sortBy, sortDirection);
		return orderService.getAllOrders(page, pageSize, sortBy, sortDirection);
	}
	
	/**
	 * @param id : The orderId
	 * @return The Order fetched
	 */
	@GetMapping("/{id}")
	@ResponseBody
    public OrderResponseDto fetchOrderById(@PathVariable Long id) {
		log.info("Fetching Order with OrderId: {}", id);
        return orderService.getOrderById(id);
    }
	
	@GetMapping("my-orders")
	@ResponseBody
	public PageResponse<OrderResponseDto> fetchMyOrders(@RequestParam(defaultValue = "0") int page){
		log.info("Fetching all orders of current user for page={}, size={}, sortBy={}, direction={}",page, pageSize, sortBy, sortDirection);
		return orderService.getMyOrders(page, pageSize, sortBy, sortDirection);
	}

	/**
	 * @param orderRequest : The OrderRequestDto
	 * @return : The order created
	 */
	@PostMapping("/create")
	@ResponseBody
	public OrderResponseDto createNewOrder(@RequestBody @Valid OrderRequestDto orderRequest) {
		log.info("Order creation request received with IdempotencyKey: {}", orderRequest.getIdempotencyKey());
		List<FlashSaleAppException> exceptions = OrderValidator.validateNewOrder(orderRequest);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		return orderService.createOrder(orderRequest.getEventId(), orderRequest.getQuantity(), orderRequest.getIdempotencyKey());
	}
	
	/**
	 * @param orderId : The Order Id
	 * @param paymentRequest : The PaymentRequestDto
	 * @return The Order confirmed
	 */
	@PutMapping("/{orderId}/confirm")
	@ResponseBody
    public OrderResponseDto confirmOrder(@PathVariable Long orderId, @RequestBody @Valid PaymentRequestDto paymentRequest) {
		log.info("Order Confirmation request received for OrderId: {} with Payment Reference Number: {}", orderId, paymentRequest.getPaymentReference());
		paymentRequest.setOrderId(orderId);
		List<FlashSaleAppException> exceptions = OrderValidator.validatePaymentRequest(paymentRequest);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
        return orderService.confirmOrder(paymentRequest);
        
    }
	
	/**
	 * @param orderId : The Order Id
	 * @return The cancelled Order
	 */
	@PutMapping("/{orderId}/cancel")
	@ResponseBody
    public OrderResponseDto cancelOrder(@PathVariable Long orderId) {
		log.info("Order Cancel request received for OrderId: {}", orderId);
        return orderService.cancelOrder(orderId);
    }
}
