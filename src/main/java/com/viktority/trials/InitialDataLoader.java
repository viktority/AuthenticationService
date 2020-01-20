package com.viktority.trials;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.viktority.trials.entities.Privilege;
import com.viktority.trials.entities.Role;
import com.viktority.trials.repositories.PrivilegeRepository;
import com.viktority.trials.repositories.RoleRepository;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	boolean alreadySetup = false;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (alreadySetup)
			return;
		Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
		Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
		Privilege deletePrivilege = createPrivilegeIfNotFound("DELETE_PRIVILEGE");
		Privilege editPrivilege = createPrivilegeIfNotFound("EDIT_PRIVILEGE");
		Privilege createPrivilege = createPrivilegeIfNotFound("CREATE_PRIVILEGE");

		List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege, deletePrivilege, editPrivilege,
				createPrivilege);
		List<Privilege> userPrivileges = Arrays.asList(readPrivilege, writePrivilege);
		createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
		createRoleIfNotFound("ROLE_USER", userPrivileges);

//		Role adminRole = roleRepository.findByName("ROLE_ADMIN");
//		Users user = new Users();
//		user.setFirstName("Test");
//		user.setLastName("Test");
//		user.setEncryptedPassword(passwordEncoder.encode("test"));
//		user.setEmail("test@test.com");
//		user.setRoles(Arrays.asList(adminRole));
//		user.setEnabled(true);
//		userRepository.save(user);

		alreadySetup = true;
	}

	@Transactional
	private Privilege createPrivilegeIfNotFound(String name) {

		Privilege privilege = privilegeRepository.findByName(name);
		if (privilege == null) {
			privilege = new Privilege(name);
			privilegeRepository.save(privilege);
		}
		return privilege;
	}

	@Transactional
	private Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {

		Role role = roleRepository.findByName(name);
		if (role == null) {
			role = new Role(name);
			role.setPrivileges(privileges);
			roleRepository.save(role);
		}
		return role;
	}
}
