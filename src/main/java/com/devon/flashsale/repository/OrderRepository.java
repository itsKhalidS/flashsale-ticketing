package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devon.flashsale.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
