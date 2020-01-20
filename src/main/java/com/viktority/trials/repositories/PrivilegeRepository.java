package com.viktority.trials.repositories;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.viktority.trials.entities.Privilege;

@Repository
public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {

	Privilege findByName(String name);

	Collection<Privilege> findAllByRolesName(String name);
}
