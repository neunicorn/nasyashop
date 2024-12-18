package com.nasya.ecommerce.security;

import com.nasya.ecommerce.common.erros.UserNotFoundException;
import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.repository.RoleRepository;
import com.nasya.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByKeyword(username)
                .orElseThrow(()-> new UserNotFoundException("USER NOT FOUND"));

        List<Role> roles = roleRepository.findByUserId(user.getUserId());

        return UserInfo.builder()
                .roles(roles)
                .user(user)
                .build();
    }
}
