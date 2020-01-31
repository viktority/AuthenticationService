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

import com.viktority.trials.entities.PasswordResetToken;
import com.viktority.trials.entities.Role;
import com.viktority.trials.entities.Users;
import com.viktority.trials.repositories.PasswordResetTokenRepository;
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
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
			Environment environment, RoleRepository roleRepository, Utils util,
			PasswordResetTokenRepository passwordResetTokenRepository) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.environment = environment;
		this.roleRepository = roleRepository;
		this.util = util;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
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
		Long expTime = Long.parseLong(environment.getProperty("email.verification.token.expiration_time"));
		userEntity.setToken(Utils.generateToken(userEntity.getId(), expTime));
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
				rm.setStatus("Activated");
				return ResponseEntity.status(HttpStatus.OK).body(rm);

			} else {
				Long expTime = Long.parseLong(environment.getProperty("email.verification.token.expiration_time"));
				String newToken = Utils.generateToken(profile.getId(), expTime);
				profile.setToken(newToken);
				usersRepository.save(profile);
				return activateProfile(newToken);
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
		roles.stream().map(p -> p.getPrivileges()).forEach(p -> p.forEach((y) -> {
			privileges.add(y.getName());
		}));

		return privileges;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
		List<GrantedAuthority> authorities = privileges.stream().map(p -> new SimpleGrantedAuthority(p))
				.collect(toList());
		return authorities;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean returnValue = false;
		Users userEntity = usersRepository.findByEmail(email);
		if (userEntity == null) {
			return returnValue;
		}
		Long expTime = Long.parseLong(environment.getProperty("password.reset.token.expiration_time"));
		String token = Utils.generateToken(userEntity.getId(), expTime);

		PasswordResetToken prte = new PasswordResetToken();
		prte.setToken(token);
		prte.setUser(userEntity);
		passwordResetTokenRepository.save(prte);

		returnValue = new AmazonSES().sendPasswordResetRequest(userEntity.getFirstName(), userEntity.getEmail(), token);

		return returnValue;
	}

	@Override
	public ResponseModel resetPassword(String token, String password) {
		ResponseModel rm = new ResponseModel();
		rm.setName("Password Reset!");
		rm.setStatus("Reset Failed!");
		boolean expired = Utils.hasTokenExpired(token);

		if (expired) {
			rm.setStatus("Token Expired!");
			return rm;
		}

		PasswordResetToken prte = passwordResetTokenRepository.findByToken(token);

		if (prte == null) {
			rm.setStatus("Token Invalid");
			return rm;
		}

		String encryptedPassword = bCryptPasswordEncoder.encode(password);
		Users user = prte.getUser();
		user.setEncryptedPassword(encryptedPassword);
		Users saved = usersRepository.save(user);

		if (saved != null && saved.getEncryptedPassword().equalsIgnoreCase(encryptedPassword)) {
			rm.setStatus("Password Updated!");
		}

		passwordResetTokenRepository.delete(prte);
		return rm;
	}

}
