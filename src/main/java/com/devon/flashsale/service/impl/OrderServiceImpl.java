package com.devon.flashsale.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.entity.Event;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.entity.Payment;
import com.devon.flashsale.enums.OrderStatus;
import com.devon.flashsale.enums.PaymentStatus;
import com.devon.flashsale.exceptions.InvalidStateException;
import com.devon.flashsale.exceptions.OutOfStockException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.exceptions.SaleWindowClosedException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.repository.OrderRepository;
import com.devon.flashsale.repository.PaymentRepository;
import com.devon.flashsale.service.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
	private final OrderRepository orderRepository;
	private final EventRepository eventRepository;
	private final PaymentRepository paymentRepository;
	
	public OrderServiceImpl(OrderRepository orderRepository, EventRepository eventRepository, PaymentRepository paymentRepository) {
		this.orderRepository = orderRepository;
		this.eventRepository = eventRepository;
		this.paymentRepository = paymentRepository;
	}

	@Override
	public List<Order> fetchAllOrders() {
		return orderRepository.findAll();
	}
	
	@Override
	public Order getOrderById(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+id+"]"));
		log.info("Order with Order Id: {} found", order.getOrderId());
		return order;
	}
	
	@Override
	@Transactional
	public Order createOrder(Long eventId, int quantity, String idempotencyKey) {
		Optional<Order> fetchedOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
		if(fetchedOrder.isPresent()) {
			log.info("Order with Idempotency Key: {} already exists", idempotencyKey);
			return fetchedOrder.get();
		}

		Event event = eventRepository.findById(eventId)
				.orElseThrow(()-> new ResourceNotFoundException("No Event Found for id ["+eventId+"]"));

		LocalDateTime currentDateTime = LocalDateTime.now();
		if(currentDateTime.isBefore(event.getStartTime()) || currentDateTime.isAfter(event.getEndTime())) {
			throw new SaleWindowClosedException("Sale Window for this event is closed");
		}

		if(event.getRemainingSeats() < quantity) {
			throw new OutOfStockException("Cannot book seats. This Event doesn't have "+quantity+" seats remaining.");
		}

		event.setRemainingSeats(event.getRemainingSeats() - quantity);
		eventRepository.save(event);
		log.info("Event with Event Id: {} updated", eventId);
		
		Order order = new Order();
		order.setEvent(event);
		order.setQty(quantity);
		order.setIdempotencyKey(idempotencyKey);
		order.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		order.setStatus(OrderStatus.PENDING);
		
		Order savedOrder = orderRepository.save(order);
		log.info("Order with Order Id: {} and Idempotency Key: {} created with Status: {}"
				, savedOrder.getOrderId(), savedOrder.getIdempotencyKey(), savedOrder.getStatus());
		return savedOrder;
	}

	@Override
	@Transactional
	public Order confirmOrder(PaymentRequestDto paymentRequest) {
		Order order = orderRepository.findById(paymentRequest.getOrderId())
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+paymentRequest.getOrderId()+"]"));
		
		if(order.getStatus() != OrderStatus.PENDING) {
			throw new InvalidStateException("The specified order is not pending.");
		}
		
		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentReference(paymentRequest.getPaymentReference());
		payment.setAmount(paymentRequest.getAmount());
		payment.setStatus(PaymentStatus.SUCCESS);
		paymentRepository.save(payment);
		log.info("Payment of Rs {} received for Order Id: {} against Payment Reference Number: {}"
				, paymentRequest.getAmount(), paymentRequest.getOrderId(), paymentRequest.getPaymentReference());
		
		order.setStatus(OrderStatus.CONFIRMED);
		Order savedOrder = orderRepository.save(order);
		log.info("Order with Order Id: {} and Idempotency Key: {} has been CONFIRMED"
				, savedOrder.getOrderId(), savedOrder.getIdempotencyKey());
		
		return savedOrder;
	}

	@Override
	@Transactional
	public Order cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+orderId+"]"));
		
		if(order.getStatus() == OrderStatus.EXPIRED) {
			throw new InvalidStateException("The specified order has expired");
		}
		
		if(order.getStatus() == OrderStatus.CANCELLED) {
			return order;
		}
		
		Event event = order.getEvent();
		event.setRemainingSeats(event.getRemainingSeats() + order.getQty());
		eventRepository.save(event);
		log.info("Event with Event Id: {} updated", event.getEventId());
		
		OrderStatus previousStatus = order.getStatus();
		
		order.setStatus(OrderStatus.CANCELLED);
		Order updatedOrder =  orderRepository.save(order);
		log.info("Order with Order Id: {} has been cancelled succesfully", orderId);
		
		if(previousStatus == OrderStatus.CONFIRMED) {
			Payment payment = updatedOrder.getPayment();
			payment.setStatus(PaymentStatus.REFUNDED);
			paymentRepository.save(payment);
			log.info("Amount of Rs {} refunded for Order Id: {} ", payment.getAmount(), orderId);			
		}
		
		return updatedOrder;
	}

}
