package com.viktority.trials.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.viktority.trials.entities.UserEntity;
import com.viktority.trials.services.models.CreateUserRequestModel;

@Service
public interface UsersService extends UserDetailsService{
	UserEntity createUser(CreateUserRequestModel userDetails);
	UserEntity getUserDetailsByEmail(String email);
	UserEntity getUserByUserId(String userId);
}
