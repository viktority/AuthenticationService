package com.viktority.trials.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.viktority.trials.entities.Users;

@Repository
public interface UsersRepository extends CrudRepository<Users, Long> {
	Users findByEmail(String email);
	Users findByUserId(String userId);
	Users findByToken(String token);
}
