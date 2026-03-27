package com.fatihozkurt.fatihozkurtcom.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Provides explicit user-details behavior to avoid default generated users.
 */
@Configuration
public class UserDetailsServiceConfig {

    /**
     * Defines user details lookup strategy.
     *
     * @return user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("UserDetailsService is not used in JWT-only flow.");
        };
    }
}

