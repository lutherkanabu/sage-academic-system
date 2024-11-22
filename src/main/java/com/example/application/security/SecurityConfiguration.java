package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {
    
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfiguration(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public resources
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                new AntPathRequestMatcher("/images/**"),
                new AntPathRequestMatcher("/frontend/**"),
                new AntPathRequestMatcher("/icons/**"),
                new AntPathRequestMatcher("/line-awesome/**"),
                new AntPathRequestMatcher("/register/**")
            ).permitAll()
        );

        // Registration page access
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(new AntPathRequestMatcher("/register")).permitAll()
        );

        // Role-based access
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(new AntPathRequestMatcher("/student/**")).hasAuthority("ROLE_STUDENT")  // Changed from hasRole to hasAuthority
            .requestMatchers(new AntPathRequestMatcher("/lecturer/**")).hasAuthority("ROLE_LECTURER")
            .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAuthority("ROLE_ADMIN")
        );

        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().requestMatchers(
            "/images/**",
            "/frontend/**",
            "/icons/**",
            "/register",
            "/register/**",
            "/line-awesome/**"
        );
    }
}