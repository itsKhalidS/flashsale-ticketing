package com.devon.flashsale.service.security.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.devon.flashsale.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.expiration}")
	private long expiration;
	
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	public String generateJwtToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", user.getUserId());
		claims.put("name", user.getName());
		claims.put("role", user.getRole());
		
		return Jwts.builder()
				.subject(user.getEmail())
				.claims(claims)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(getSigningKey())
				.compact();
	}
	
	private Claims extractAllClaims(String token) {
	    return Jwts.parser()
	            .verifyWith(getSigningKey())
	            .build()
	            .parseSignedClaims(token)
	            .getPayload();
	}
	
	public String extractUsername(String token) {
	    return extractAllClaims(token).getSubject();
	}
	
	private boolean isTokenExpired(String token) {
	    return extractAllClaims(token)
	            .getExpiration()
	            .before(new Date());
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
	    String username = extractUsername(token);
	    return !isTokenExpired(token) && username.equals(userDetails.getUsername());
	}
	
	public String extractRole(String token) {
	    return extractAllClaims(token)
	            .get("role", String.class);
	}
	
	public String extractName(String token) {
	    return extractAllClaims(token)
	            .get("name", String.class);
	}
}
