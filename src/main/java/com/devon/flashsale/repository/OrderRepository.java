package com.devon.flashsale.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devon.flashsale.entity.Order;
import java.util.List;
import com.devon.flashsale.enums.OrderStatus;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
	
	public Optional<Order> findByIdempotencyKey(String idempotencyKey);
	
	public List<Order> findAllByStatus(OrderStatus status);
}
