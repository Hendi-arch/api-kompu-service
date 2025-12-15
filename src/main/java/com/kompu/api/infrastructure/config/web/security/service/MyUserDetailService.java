package com.kompu.api.infrastructure.config.web.security.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.usecase.user.GetUserUseCase;

@Service
public class MyUserDetailService implements UserDetailsService {

	private final GetUserUseCase getUserUseCase;

	public MyUserDetailService(GetUserUseCase getUserUseCase) {
		this.getUserUseCase = getUserUseCase;
	}

	@Override
	public UserDetails loadUserByUsername(String identity) {
		UserAccountModel userAccount;

		// Try to identify if it's a UUID (UserId)
		try {
			UUID userId = UUID.fromString(identity);
			userAccount = getUserUseCase.findById(userId);
		} catch (IllegalArgumentException e) {
			// Not a UUID, assume it's an email
			userAccount = getUserUseCase.findByEmail(identity);
		}

		String userIdString = userAccount.getId().toString();
		String password = userAccount.getPasswordHash();

		List<String> authorities = new ArrayList<>();
		userAccount.getRoles().forEach(role -> authorities.add("ROLE_" + role.getName()));

		if (authorities.isEmpty()) {
			authorities.add("ROLE_USER");
		}

		return User
				.withUsername(userIdString) // Principal is UserId
				.password(password)
				.authorities(authorities.toArray(String[]::new))
				.build();
	}

}
