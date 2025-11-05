package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devon.flashsale.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
