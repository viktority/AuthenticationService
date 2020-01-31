package com.viktority.trials.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viktority.trials.entities.Users;
import com.viktority.trials.services.UsersService;
import com.viktority.trials.services.models.CreateUserRequestModel;
import com.viktority.trials.services.models.LoginRequestModel;
import com.viktority.trials.services.models.PasswordResetModel;
import com.viktority.trials.services.models.PasswordResetRequestModel;
import com.viktority.trials.services.models.ResponseModel;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/users")
public class UsersController {

	@Autowired
	private Environment env;

	@Autowired
	UsersService usersService;

	@GetMapping("/status/check")
	@ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", value = "token", paramType = "header") })
	@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
	// @Secured("CREATE_PRIVILEGE")
	public String status() {
		return "Working on port " + env.getProperty("local.server.port") + ", with token = "
				+ env.getProperty("token.secret");
	}

	@PostMapping(path = "/signup", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })

	@ApiOperation(value = "get all activity", notes = "")
	public ResponseEntity<Users> createUser(@RequestBody CreateUserRequestModel userDetails) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		Users createdUser = usersService.createUser(userDetails);

		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

	@ApiOperation(value = "get all activity", notes = "")
	@ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", value = "token", paramType = "header") })
	@GetMapping(value = "/{userId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
	public ResponseEntity<Users> getUser(@PathVariable("userId") String userId) {

		Users userDto = usersService.getUserByUserId(userId);

		return ResponseEntity.status(HttpStatus.OK).body(userDto);
	}

	@ApiOperation(value = "get all activity", notes = "")
	@ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", value = "token", paramType = "header") })
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@PreAuthorize("hasAuthority('EDIT_PRIVILEGE')")
	public ResponseEntity<List<Users>> getAllUser() {

		List<Users> userDto = usersService.getUsers();

		return ResponseEntity.status(HttpStatus.OK).body(userDto);
	}

	@ApiOperation("User login")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/login")
	public void Login(@RequestBody LoginRequestModel loginRequestModel) {
		throw new IllegalStateException("This Method should not be called!");
	}

	@ApiOperation(value = "Activate account with generated Token!", notes = "")
	@GetMapping(path = "/activate", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> activateAccount(@RequestParam("token") String token) {

		return usersService.activateProfile(token);
	}

	@ApiOperation(value = "Send mail to request for a generated Token for password reset!", notes = "")
	@PostMapping(path = "/password-reset-request", produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Boolean> requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {

		boolean operationResult = usersService.requestPasswordReset(passwordResetRequestModel.getEmail());

		return ResponseEntity.status(HttpStatus.OK).body(operationResult);
	}

	@PostMapping(path = "/password-reset", consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> resetPassword(@RequestBody PasswordResetModel passwordResetModel) {

		ResponseModel operationResult = usersService.resetPassword(passwordResetModel.getToken(),
				passwordResetModel.getPassword());

		return ResponseEntity.status(HttpStatus.OK).body(operationResult);
	}
}
