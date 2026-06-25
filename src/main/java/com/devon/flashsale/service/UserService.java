package com.devon.flashsale.service;

import com.devon.flashsale.dto.LoginUserDto;
import com.devon.flashsale.dto.RegisterUserDto;
import com.devon.flashsale.dto.UserProfileDto;
import com.devon.flashsale.entity.User;

public interface UserService {
	
	public User createNewUser(RegisterUserDto newUserDetails);

	public User loginUser(LoginUserDto userDetails);

	UserProfileDto getCurrentUserProfile();

}
