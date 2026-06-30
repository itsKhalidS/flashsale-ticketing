package com.devon.flashsale.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.devon.flashsale.config.FlashSaleMetricsConfig;
import com.devon.flashsale.dto.OrderResponseDto;
import com.devon.flashsale.dto.PaymentRequestDto;
import com.devon.flashsale.entity.Event;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.entity.Payment;
import com.devon.flashsale.entity.User;
import com.devon.flashsale.enums.OrderStatus;
import com.devon.flashsale.enums.PaymentStatus;
import com.devon.flashsale.enums.UserRole;
import com.devon.flashsale.exceptions.InvalidStateException;
import com.devon.flashsale.exceptions.OutOfStockException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.exceptions.SaleWindowClosedException;
import com.devon.flashsale.exceptions.ValidationException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.repository.OrderRepository;
import com.devon.flashsale.repository.PaymentRepository;
import com.devon.flashsale.repository.UserRepository;
import com.devon.flashsale.service.OrderService;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final EventRepository eventRepository;
	private final PaymentRepository paymentRepository;
	private final FlashSaleMetricsConfig flashSaleMetricsConfig;
	
	private final int MAX_ATTEMPTS = 3;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	
	public OrderServiceImpl(UserRepository userRepository, OrderRepository orderRepository, EventRepository eventRepository, PaymentRepository paymentRepository, FlashSaleMetricsConfig flashSaleMetricsConfig) {
		this.userRepository = userRepository;
		this.orderRepository = orderRepository;
		this.eventRepository = eventRepository;
		this.paymentRepository = paymentRepository;
		this.flashSaleMetricsConfig = flashSaleMetricsConfig;
	}
	
	private User getCurrentUser() {
	    String email = SecurityContextHolder.getContext().getAuthentication().getName();
	    return userRepository.findByEmail(email)
	    		.orElseThrow(() -> new ResourceNotFoundException( "User Authentication Error" ));
	}
	
	private void validateOrderAccess(Order order) {
	    User currentUser = getCurrentUser();
	    if(currentUser.getRole() == UserRole.ADMIN) {
	        return;
	    }
	    if(!order.getUser().getEmail().equals(currentUser.getEmail())) {
	        throw new AuthorizationDeniedException("Invalid order details provided");
	    }
	}
	
	private OrderResponseDto convertToDto(Order order) {
	    OrderResponseDto orderResponseDto = new OrderResponseDto();
	    orderResponseDto.setOrderId(order.getOrderId());
	    orderResponseDto.setEventId(order.getEvent().getEventId());
	    orderResponseDto.setEventName(order.getEvent().getEventName());
	    orderResponseDto.setQuantity(order.getQty());
	    orderResponseDto.setStatus(order.getStatus());
	    if(order.getPayment() != null) {
	    	orderResponseDto.setPaymentReference(order.getPayment().getPaymentReference());
	    }
	    orderResponseDto.setTicketNumber(order.getTicketNumber());
	    orderResponseDto.setCreatedAt(order.getCreatedAt());
	    orderResponseDto.setExpiresAt(order.getExpiresAt());

	    return orderResponseDto;
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public List<OrderResponseDto> getAllOrders() {
		return orderRepository.findAll().stream().map(this::convertToDto).toList();
	}
	
	@Override
	public OrderResponseDto getOrderById(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+id+"]"));
		log.info("Order with Order Id: {} found", order.getOrderId());
		validateOrderAccess(order);
		return convertToDto(order);
	}
	
	@Override
	public List<OrderResponseDto> getMyOrders() {
		User currentUser = getCurrentUser();
	    List<Order> orders = orderRepository.findByUser(currentUser);

	    return orders.stream().map(this::convertToDto).toList();
	}
	
	@Override
	@Transactional
	public OrderResponseDto createOrder(Long eventId, int quantity, String idempotencyKey) {
		Optional<Order> fetchedOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
		if(fetchedOrder.isPresent()) {
			log.info("Order with Idempotency Key: {} already exists", idempotencyKey);
			return convertToDto(fetchedOrder.get());
		}

		Event event = eventRepository.findById(eventId)
				.orElseThrow(()-> new ResourceNotFoundException("No Event Found for id ["+eventId+"]"));

		LocalDateTime currentDateTime = LocalDateTime.now();
		if(currentDateTime.isBefore(event.getStartTime()) || currentDateTime.isAfter(event.getEndTime())) {
			throw new SaleWindowClosedException("Sale Window for this event is closed");
		}
		
		int attempt = 0;
		boolean success = false;
		
		while(attempt < MAX_ATTEMPTS) {
			try {
				attempt++;
				event = eventRepository.findById(eventId)
						.orElseThrow(()-> new ResourceNotFoundException("No Event Found for id ["+eventId+"]"));
				
				if(event.getRemainingSeats() < quantity) {
					throw new OutOfStockException("Cannot book seats. This Event doesn't have "+quantity+" seats remaining.");
				}
				
				event.setRemainingSeats(event.getRemainingSeats() - quantity);
				eventRepository.save(event);
				success=true;
				log.info("Event with Event Id: {} updated", eventId);
				break;
			} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
				if(attempt < MAX_ATTEMPTS) {
					log.error("OptimisticLockException encountered. Trying again");
					flashSaleMetricsConfig.incrementOptimisticLockRetries();
				}
			}
			
		}
		
		if(!success) {
			throw new ValidationException("Unable to create order. System is busy");
		}
		
		Order order = new Order();
		order.setUser(getCurrentUser());
		order.setEvent(event);
		order.setQty(quantity);
		order.setIdempotencyKey(idempotencyKey);
		order.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		order.setStatus(OrderStatus.PENDING);
		
		Order savedOrder = null;
		try{
			savedOrder = orderRepository.save(order);
			log.info("Order with Order Id: {} and Idempotency Key: {} created with Status: {}"
					, savedOrder.getOrderId(), savedOrder.getIdempotencyKey(), savedOrder.getStatus());
			flashSaleMetricsConfig.incrementOrdersCreated();
		} catch(ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			log.error("Failed in  creating order with Idempotency Key: ["+idempotencyKey+"]");
		}
		return convertToDto(savedOrder);
	}

	@Override
	@Transactional
	public OrderResponseDto createOrderWithoutOptimisticLock(Long eventId, int quantity, String idempotencyKey) {
		Optional<Order> fetchedOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
		if(fetchedOrder.isPresent()) {
			log.info("Order with Idempotency Key: {} already exists", idempotencyKey);
			return convertToDto(fetchedOrder.get());
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
		
		int rowsEffected = eventRepository.decrementSeatsIfAvailable(event.getEventId(), quantity);
		if(rowsEffected == 0) {
			throw new OutOfStockException("Cannot book seats. This Event doesn't have "+quantity+" seats remaining.");
		}		
		log.info("Event with Event Id: {} updated", eventId);
		
		Order order = new Order();
		order.setUser(getCurrentUser());
		order.setEvent(event);
		order.setQty(quantity);
		order.setIdempotencyKey(idempotencyKey);
		order.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		order.setStatus(OrderStatus.PENDING);		
		
		Order savedOrder = null;
		try{
			savedOrder = orderRepository.save(order);
			log.info("Order with Order Id: {} and Idempotency Key: {} created with Status: {}"
					, savedOrder.getOrderId(), savedOrder.getIdempotencyKey(), savedOrder.getStatus());
			flashSaleMetricsConfig.incrementOrdersCreated();
		} catch(ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			log.error("Failed in  creating order with Idempotency Key: ["+idempotencyKey+"]");
		}
		
		return convertToDto(savedOrder);
	}

	@Override
	@Transactional
	public OrderResponseDto confirmOrder(PaymentRequestDto paymentRequest) {
		Order order = orderRepository.findById(paymentRequest.getOrderId())
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+paymentRequest.getOrderId()+"]"));
		validateOrderAccess(order);
		if(order.getStatus() != OrderStatus.PENDING) {
			throw new InvalidStateException("The specified order is not pending.");
		}
		
		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentReference(paymentRequest.getPaymentReference());
		payment.setAmount(paymentRequest.getAmount());
		payment.setStatus(PaymentStatus.SUCCESS);
		Payment savedPayment = paymentRepository.save(payment);
		log.info("Payment of Rs {} received for Order Id: {} against Payment Reference Number: {}"
				, paymentRequest.getAmount(), paymentRequest.getOrderId(), paymentRequest.getPaymentReference());
		
		order.setStatus(OrderStatus.CONFIRMED);
		order.setTicketNumber(generateTicketNumber(order.getOrderId()));
		Order savedOrder = null;
		try {
			savedOrder = orderRepository.save(order);
			log.info("Order with Order Id: {}, Ticket Number: {} and Idempotency Key: {} has been CONFIRMED"
					, savedOrder.getOrderId(), savedOrder.getTicketNumber(), savedOrder.getIdempotencyKey());
		} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			throw new ValidationException("Cannot confirm order with Order Id: ["+order.getOrderId()+"]");
		}
		order.setPayment(savedPayment);
		return convertToDto(savedOrder);
	}

	@Override
	@Transactional
	public OrderResponseDto cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+orderId+"]"));
		validateOrderAccess(order);
		if(order.getStatus() == OrderStatus.EXPIRED) {
			throw new InvalidStateException("The specified order has expired");
		}
		
		if(order.getStatus() == OrderStatus.CANCELLED) {
			return convertToDto(order);
		}
		
		Event event;
		int attempt=0;
		boolean success=false;
		while(attempt < MAX_ATTEMPTS) {
			try {
				attempt++;	
				
				event = eventRepository.findById(order.getEvent().getEventId())
						.orElseThrow(()-> new ResourceNotFoundException("No Event Found for id ["+order.getEvent().getEventId()+"]"));
				
				event.setRemainingSeats(event.getRemainingSeats() + order.getQty());
				eventRepository.save(event);
				success=true;
				log.info("Event with Event Id: {} updated", event.getEventId());
				break;
				
			} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
				if(attempt < MAX_ATTEMPTS) {
					log.error("OptimisticLockException encountered. Trying again");
					flashSaleMetricsConfig.incrementOptimisticLockRetries();
				}
			}
			
		}
		
		if(!success) {
			throw new ValidationException("Unable to cancel order. System is busy");
		}
		
		OrderStatus previousStatus = order.getStatus();
		
		order.setStatus(OrderStatus.CANCELLED);
		
		Order updatedOrder =  null;
		try {
			updatedOrder = orderRepository.save(order);
			log.info("Order with Order Id: {} has been cancelled succesfully", orderId);
		} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			throw new ValidationException("Cannot cancel order with Order Id: ["+order.getOrderId()+"]");
		}
		
		if(previousStatus == OrderStatus.CONFIRMED) {
			Payment payment = updatedOrder.getPayment();
			payment.setStatus(PaymentStatus.REFUNDED);
			paymentRepository.save(payment);
			log.info("Amount of Rs {} refunded for Order Id: {} ", payment.getAmount(), orderId);			
		}
		
		return convertToDto(updatedOrder);
	}

	@Override
	@Transactional
	public OrderResponseDto cancelOrderWithoutOptimisticLock(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(()-> new ResourceNotFoundException("No Order Found for id ["+orderId+"]"));
		validateOrderAccess(order);
		if(order.getStatus() == OrderStatus.EXPIRED) {
			throw new InvalidStateException("The specified order has expired");
		}
		
		if(order.getStatus() == OrderStatus.CANCELLED) {
			return convertToDto(order);
		}
		
		Event event = order.getEvent();
		int rowsEffected = eventRepository.incrementSeats(event.getEventId(), order.getQty());
		if(rowsEffected == 0) {
			throw new ValidationException("Error during order cancellation");
		}
		log.info("Event with Event Id: {} updated", event.getEventId());
		
		OrderStatus previousStatus = order.getStatus();
		
		order.setStatus(OrderStatus.CANCELLED);
		Order updatedOrder =  null;
		try {
			updatedOrder = orderRepository.save(order);
			log.info("Order with Order Id: {} has been cancelled succesfully", orderId);
		} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			throw new ValidationException("Cannot cancel order with Order Id: ["+order.getOrderId()+"]");
		}		
		
		log.info("Order with Order Id: {} has been cancelled succesfully", orderId);
		
		if(previousStatus == OrderStatus.CONFIRMED) {
			Payment payment = updatedOrder.getPayment();
			payment.setStatus(PaymentStatus.REFUNDED);
			paymentRepository.save(payment);
			log.info("Amount of Rs {} refunded for Order Id: {} ", payment.getAmount(), orderId);			
		}
		
		return convertToDto(updatedOrder);
	}
	
	@Override
	public List<Order> getAllOrdersByStatus(OrderStatus status) {
		log.info("Fetching all orders having status = {} ", status.toString());
		return orderRepository.findAllByStatus(status);
	}
	
	@Override
	@Transactional
	public void expirePendingOrder(Order order) {
		
		Event event;
		int attempt=0;
		boolean success=false;
		while(attempt < MAX_ATTEMPTS) {
			try {
				attempt++;	
				
				event = eventRepository.findById(order.getEvent().getEventId())
						.orElseThrow(()-> new ResourceNotFoundException("No Event Found for id ["+order.getEvent().getEventId()+"]"));
				
				event.setRemainingSeats(event.getRemainingSeats() + order.getQty());
				eventRepository.save(event);
				success=true;
				log.info("Event with Event Id: {} updated", event.getEventId());
				break;
				
			} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
				if(attempt < MAX_ATTEMPTS) {
					log.error("OptimisticLockException encountered. Trying again");
					flashSaleMetricsConfig.incrementOptimisticLockRetries();
				}
			}
		}
		if(!success) {
			log.error("Failed in expiring order with OrderId: ["+order.getOrderId()+"]");
			return;
		}
		
		order.setStatus(OrderStatus.EXPIRED);
		try {
			orderRepository.save(order);
			log.info("Status updated to [{}] for Order with Order Id: {}", OrderStatus.EXPIRED, order.getOrderId());
		}catch(ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			log.error("Failed in expiring order with OrderId: ["+order.getOrderId()+"]");
		}
	}
	
	@Override
	@Transactional
	public void expirePendingOrderWithoutOptimisticLock(Order order) {
		Event event = order.getEvent();
		int rowsEffected = eventRepository.incrementSeats(event.getEventId(), order.getQty());
		if(rowsEffected == 0) {
			log.error("Failed in expiring order with OrderId: ["+order.getOrderId()+"]");
			return;
		}
		log.info("Event with Event Id: {} updated", event.getEventId());
		
		order.setStatus(OrderStatus.EXPIRED);
		try {
			orderRepository.save(order);
			log.info("Status updated to [{}] for Order with Order Id: {}", OrderStatus.EXPIRED, order.getOrderId());
		}catch(ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			log.error("Failed in expiring order with OrderId: ["+order.getOrderId()+"]");
		}
	}

	@Override
	public String generateTicketNumber(Long orderId) {
        String date = LocalDate.now().format(DATE_FORMAT);

        return String.format("TID-%s-%08d", date, orderId);
    }

}
