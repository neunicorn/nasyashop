package com.nasya.ecommerce.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nasya.ecommerce.common.erros.UserNotFoundException;
import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.repository.RoleRepository;
import com.nasya.ecommerce.repository.UserRepository;
import com.nasya.ecommerce.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService {

    private final String USER_CACHE_KEY = "cache:user:";
    private final String USER_ROLES_CACHE_KEY = "cache:user:role:";
    private final CacheService cacheService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //get user from redis
        String userCacheKey = USER_CACHE_KEY + username;
        String userRolesCacheKey = USER_ROLES_CACHE_KEY + username;

        Optional<User> userOpt = cacheService.get(userCacheKey, User.class);
        Optional <List<Role>> rolesOpt = cacheService.get(userRolesCacheKey, new TypeReference<List<Role>>() {
        });

        // return if the user already login and the user already cached
        if(userOpt.isPresent() && rolesOpt.isPresent()) {
            return UserInfo.builder()
                    .roles(rolesOpt.get())
                    .user(userOpt.get())
                    .build();
        }

        User user = userRepository.findByKeyword(username)
                .orElseThrow(()-> new UserNotFoundException("USER NOT FOUND"));

        List<Role> roles = roleRepository.findByUserId(user.getUserId());

        // cached new logged user
        cacheService.put(USER_CACHE_KEY + user.getUserId(), user);
        cacheService.put(USER_ROLES_CACHE_KEY + user.getUserId(), roles);

        return UserInfo.builder()
                .roles(roles)
                .user(user)
                .build();
    }
}
