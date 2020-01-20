package com.viktority.trials.utils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;

@Service
public class Utils {

	@Autowired
	Environment env;

	private static final Key secret = MacProvider.generateKey(SignatureAlgorithm.HS256);
	private static final byte[] secretBytes = secret.getEncoded();
	private static final String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);

	public static String getTokenSecret() {
		return base64SecretBytes;
	}

	public String generateEmailVerificationToken(Long userId) {
		String token = Jwts.builder().setSubject(userId.toString())
				.setExpiration(new Date(System.currentTimeMillis()
						+ Long.parseLong(env.getProperty("email.verification.token.expiration_time"))))
				.signWith(SignatureAlgorithm.HS512, getTokenSecret()).compact();
		return token;
	}

	public static boolean hasTokenExpired(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(getTokenSecret()).parseClaimsJws(token).getBody();

			Date tokenExpirationDate = claims.getExpiration();
			Date today = new Date();

			return tokenExpirationDate.before(today);
		} catch (SignatureException ex) {
			return true;
		}
	}
}
