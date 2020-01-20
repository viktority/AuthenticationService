package com.viktority.trials.repositories;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.viktority.trials.entities.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

	Role findByName(String name);
	Collection<Role> findAllByUsersUserId(String userId);

}
