package com.kompu.api.infrastructure.config.web.security.service;

import java.util.ArrayList;
import java.util.List;

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
		UserAccountModel userAccount = getUserUseCase.findByUsername(identity);
		String username = userAccount.getUsername();
		String password = userAccount.getPasswordHash();

		List<String> authorities = new ArrayList<>();
		userAccount.getRoles().forEach(role -> authorities.add("ROLE_" + role.getName()));

		if (authorities.isEmpty()) {
			authorities.add("ROLE_USER");
		}

		return User
				.withUsername(username)
				.password(password)
				.authorities(authorities.toArray(new String[authorities.size()]))
				.build();
	}

}
