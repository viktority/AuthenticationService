package com.viktority.trials.services.impls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import static java.util.stream.Collectors.toList;

import javax.persistence.EntityExistsException;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.viktority.trials.entities.Privilege;
import com.viktority.trials.entities.Role;
import com.viktority.trials.entities.Users;
import com.viktority.trials.repositories.RoleRepository;
import com.viktority.trials.repositories.UsersRepository;
import com.viktority.trials.services.UsersService;
import com.viktority.trials.services.models.CreateUserRequestModel;
import com.viktority.trials.services.models.ResponseModel;
import com.viktority.trials.utils.AmazonSES;
import com.viktority.trials.utils.Utils;

@Service
public class UsersServiceImpl implements UsersService {

	UsersRepository usersRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	Environment environment;
	RoleRepository roleRepository;
	Utils util;

	@Autowired
	public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
			Environment environment, RoleRepository roleRepository, Utils util) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.environment = environment;
		this.roleRepository = roleRepository;
		this.util = util;
	}

	@Override
	public Users createUser(CreateUserRequestModel userDetails) {
		// TODO Auto-generated method stub

		if (emailExist(userDetails.getEmail())) {
			throw new EntityExistsException("There is an account with that email adress: " + userDetails.getEmail());
		}
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		Users userEntity = modelMapper.map(userDetails, Users.class);
		userEntity.setUserId(UUID.randomUUID().toString());
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
		userEntity.setRoles(Arrays.asList(roleRepository.findByName("ROLE_ADMIN")));
		userEntity.setTokenExpired(false);
		userEntity.setEnabled(true);
		userEntity.setToken(util.generateEmailVerificationToken(userEntity.getId()));
		userEntity.setActive(false);
		Users returnValue = usersRepository.save(userEntity);
		new AmazonSES().verifyEmail(returnValue);
		return returnValue;
	}

	private boolean emailExist(String email) {
		Users user = usersRepository.findByEmail(email);
		return user != null;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users userEntity = usersRepository.findByEmail(username);

		if (userEntity == null)
			throw new UsernameNotFoundException(username);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), userEntity.isActive(), true, true,
				true, getAuthorities(userEntity.getRoles()));
	}

	@Override
	public Users getUserDetailsByEmail(String email) {
		Users userEntity = usersRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return userEntity;
	}

	@Override
	public Users getUserByUserId(String userId) {

		Users userEntity = usersRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User not found");

		return userEntity;
	}

	@Override
	public List<Users> getUsers() {

		return (List<Users>) usersRepository.findAll();
	}

	@Override
	public Collection<Role> getUserRoles(String email) {

		Users userEntity = usersRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException("User not found");

		return userEntity.getRoles();
	}

	@Override
	public ResponseEntity<ResponseModel> activateProfile(String token) {
		Users profile = usersRepository.findByToken(token);
		ResponseModel rm = new ResponseModel();
		rm.setName("Account Activation");
		rm.setStatus("Account doesn't exist!");
		if (profile != null) {

			boolean hasTokenExpired = Utils.hasTokenExpired(token);
			if (!hasTokenExpired) {

				profile.setActive(true);
				profile.setToken(null);
				usersRepository.save(profile);
				rm.setName("Account Activation");
				rm.setStatus("Activated");
				return ResponseEntity.status(HttpStatus.OK).body(rm);

			} else {
				// rm.setStatus("Expired");
				String newToken = util.generateEmailVerificationToken(profile.getId());
				profile.setToken(newToken);
				usersRepository.save(profile);
				return activateProfile(newToken);
				// return ResponseEntity.status(HttpStatus.OK).body(rm);
			}

		} else {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(rm);
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {

		return getGrantedAuthorities(getPrivileges(roles));
	}

	private List<String> getPrivileges(Collection<Role> roles) {
		List<String> privileges = new ArrayList<>();
//		//List<Collection<Privilege>> privilegess = 
//				roles.stream()
//				.map(p -> p.getPrivileges())
//				//.collect(toList());
//		//privilegess
//		//.stream()
//		.forEach(p -> p.forEach((y) -> {
//			privileges.add(y.getName());
//		}));

		roles.stream().map(p -> p.getPrivileges()).forEach(p -> p.forEach((y) -> {
			privileges.add(y.getName());
		}));

//		List<Privilege> collection = new ArrayList<>();
//
//		for (Role role : roles) {
//			collection.addAll(role.getPrivileges());
//		}
//		for (Privilege item : collection) {
//			privileges.add(item.getName());
//		}
		return privileges;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
		List<GrantedAuthority> authorities = privileges.stream().map(p -> new SimpleGrantedAuthority(p))
				.collect(toList());
		return authorities;
	}

}
