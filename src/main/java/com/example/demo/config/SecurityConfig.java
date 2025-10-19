package com.example.demo.config;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration class that defines access rules,
 * custom login behavior, password encoding, and user details resolution.
 */
@Configuration
@EnableMethodSecurity  //  Enables use of @PreAuthorize in controllers
public class SecurityConfig {

    /**
     * Defines the security filter chain that sets up access control, login, logout, session management, etc.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //  Authorization rules for different URL patterns

                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints
                        .requestMatchers("/", "/products/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/register", "/login").permitAll()
                        // Admin-only endpoint
                        .requestMatchers("/admin/messages").hasRole("ADMIN")
                        // All other /admin/** paths require ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                //  Custom form login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/login/success", true)
                        .failureUrl("/login?error=true")
                )
                //  Logout configuration
                .logout(logout -> logout
                        .logoutSuccessUrl("/")  // Redirect to home page after logout
                        .permitAll()
                )

                //  Session management policy
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                //  "Remember Me" functionality for persistent login

                .rememberMe(r -> r
                        .key("demo-shop-remember")
                        .tokenValiditySeconds(7 * 24 * 60 * 60))

                //  Allow H2-console or iframe content from same origin (used for admin tools/testing)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                //  CSRF protection disabled (acceptable for internal apps / dev only)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Provides the password encoder used for user authentication.
     * BCrypt is a secure and recommended password hashing algorithm.
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Custom UserDetailsService that loads user data from the database (or a hardcoded admin user).
     *
     * @param accountRepository repository to look up users by username
     * @return a UserDetailsService that Spring Security uses for authentication
     */
    @Bean
    public UserDetailsService users(AccountRepository accountRepository) {
        return username -> {
            //  Special case: hardcoded admin user
            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password(passwordEncoder().encode("admin123"))
                        .roles("ADMIN")
                        .build();
            }

            //  Lookup regular user from DB
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Clean up role name (remove "ROLE_" prefix if present)
            String role = account.getRole() != null ? account.getRole().replace("ROLE_", "") : "USER";

            // Return UserDetails object used by Spring Security
            return User.withUsername(account.getUsername())
                    .password(account.getPasswordHash())
                    .roles(role)
                    .build();
        };
    }
}
