package com.iubh.quizbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http) throws Exception {
//        http
//                // 1. Disable CSRF protection, as we are using stateless JWT authentication.
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // 2. Define authorization rules for HTTP requests.
//                .authorizeHttpRequests(auth -> auth
//                   //     .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//                        .requestMatchers(
//                                "/",
//                                "/index.html",
//                                "/vite.svg",
//                                "/assets/**",
//                                "/static/**",
//                                "/*.ico",
//                                "/*.png",
//                                "/*.json",
//                                "/*.js",
//                                "/*.css"
//                        ).permitAll()
//                        .requestMatchers(
//                                "/api/v1/auth/authenticate",
//                                "/api/v1/auth/register",
//                                "/api/v1/auth/refresh-token"
//                        ).permitAll()
//                        .requestMatchers("/ws-connect/**").permitAll()
//                       // .requestMatchers("/api/**").authenticated()
//                        .anyRequest().authenticated()
//                )
//
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .requestCache(AbstractHttpConfigurer::disable)
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }





    @Bean @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/authenticate", "/api/v1/auth/register", "/api/v1/auth/refresh-token").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean @Order(2)
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}