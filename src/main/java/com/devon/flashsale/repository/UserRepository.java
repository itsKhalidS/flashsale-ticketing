package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devon.flashsale.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	
	public Optional<User> findByEmail(String email);
}
