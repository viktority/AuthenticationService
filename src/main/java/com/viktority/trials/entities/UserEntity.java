package com.viktority.trials.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity(name="users")
@Data
public class UserEntity implements Serializable {
 
	private static final long serialVersionUID = -2731425678149216053L;
	
	@Id
	@GeneratedValue
	@JsonIgnore
	private long id;
	
	@Column(nullable=false, length=50)
	private String firstName;
	
	@Column(nullable=false, length=50)
	private String lastName;
	
	@Column(nullable=false, length=120, unique=true)
	private String email;
	
	@Column(nullable=false, unique=true)
	private String userId;
	
	@Column(nullable=false, unique=true)	
	@JsonIgnore
	private String encryptedPassword;

}
