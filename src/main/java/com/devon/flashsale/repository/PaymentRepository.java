package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devon.flashsale.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
