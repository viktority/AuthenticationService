package com.viktority.trials.repositories;

import org.springframework.data.repository.CrudRepository;

import com.viktority.trials.entities.UserEntity;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
}
