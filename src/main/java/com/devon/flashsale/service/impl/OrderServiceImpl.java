package com.devon.flashsale.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.enums.OrderStatus;
import com.devon.flashsale.exceptions.InvalidStateException;
import com.devon.flashsale.exceptions.OutOfStockException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.exceptions.SaleWindowClosedException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.repository.OrderRepository;
import com.devon.flashsale.service.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final EventRepository eventRepository;
	
	public OrderServiceImpl(OrderRepository orderRepository, EventRepository eventRepository) {
		this.orderRepository = orderRepository;
		this.eventRepository = eventRepository;
	}

	@Override
	public List<Order> fetchAllOrders() {
		return orderRepository.findAll();
	}
	
	@Override
	public Order fetchOrderById(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+id+"]"));
	}
	
	@Override
	@Transactional
	public Order createOrder(Long eventId, int quantity, String idempotencyKey) {
		Optional<Order> fetchedOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
		if(fetchedOrder.isPresent()) {
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
		
		Order order = new Order();
		order.setEvent(event);
		order.setQty(quantity);
		order.setIdempotencyKey(idempotencyKey);
		order.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		order.setStatus(OrderStatus.PENDING);
		
		return orderRepository.save(order);
				
		
		//Auto Expiration Logic
	}

	@Override
	@Transactional
	public Order confirmOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+orderId+"]"));
		if(order.getStatus() != OrderStatus.PENDING) {
			throw new InvalidStateException("The specified order is not pending.");
		}
		order.setStatus(OrderStatus.CONFIRMED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public Order cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+orderId+"]"));
		
		if(order.getStatus() == OrderStatus.EXPIRED) {
			throw new InvalidStateException("The specified order has already expired");
		}
		
		if(order.getStatus() == OrderStatus.CANCELLED) {
			return order;
		}
		
		Event event = order.getEvent();
		event.setRemainingSeats(event.getRemainingSeats() + order.getQty());
		eventRepository.save(event);
		
		//cancel payment for confirmed order
		
		order.setStatus(OrderStatus.CANCELLED);
		return orderRepository.save(order);
	}

}
