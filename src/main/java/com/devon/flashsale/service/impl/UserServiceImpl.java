package com.devon.flashsale.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.devon.flashsale.dto.RegisterUserDto;
import com.devon.flashsale.entity.User;
import com.devon.flashsale.repository.UserRepository;
import com.devon.flashsale.service.UserService;

import jakarta.validation.ValidationException;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	public static final String USER_ROLE = "USER";
	
	public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	@Override
	public User createNewUser(RegisterUserDto newUserDetails) {
		Optional<User> existingUser = userRepository.findByEmail(newUserDetails.getEmail());
		if(existingUser.isPresent()) {
			log.info("User with email: {} already exists", newUserDetails.getEmail());
			throw new ValidationException("User with this email already exists");
		}
		
		User newUser = new User();
		newUser.setName(newUserDetails.getName());
		newUser.setEmail(newUserDetails.getEmail());
		newUser.setPassword(passwordEncoder.encode(newUserDetails.getPassword()));
		newUser.setRole(USER_ROLE);
		newUser = userRepository.save(newUser);
		log.info("New user with User Id: "+newUser.getUserId()+", Name: "+newUser.getName()+" and Email: "+newUser.getEmail()+" has been created");
		return newUser;
	}

}
