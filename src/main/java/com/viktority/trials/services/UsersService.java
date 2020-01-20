package com.viktority.trials.services;

import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.viktority.trials.entities.Role;
import com.viktority.trials.entities.Users;
import com.viktority.trials.services.models.CreateUserRequestModel;
import com.viktority.trials.services.models.ResponseModel;

@Service
public interface UsersService extends UserDetailsService {
	Users createUser(CreateUserRequestModel userDetails);

	Users getUserDetailsByEmail(String email);

	Users getUserByUserId(String userId);

	public Collection<Role> getUserRoles(String email);

	Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles);

	List<Users> getUsers();

	public ResponseEntity<ResponseModel> activateProfile(String token);
}
