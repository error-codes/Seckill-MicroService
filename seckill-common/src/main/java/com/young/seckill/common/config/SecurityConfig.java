package com.young.seckill.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.young.seckill.common.handler.JwtAuthenticationFilter;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.response.RespResult;
import jakarta.servlet.ServletOutputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper            objectMapper;

    @Lazy
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.requestMatchers("/user/login")
                                                               .permitAll()
                                                               .requestMatchers("/user/register")
                                                               .permitAll()
                                                               .anyRequest()
                                                               .authenticated())
                    .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler())
                                                             .authenticationEntryPoint(authenticationEntryPoint()))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(registry -> registry.loginProcessingUrl("user/login"))
                    .headers(headers -> headers.frameOptions(options -> options.sameOrigin().disable()));
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(RespCode.UN_AUTHORIZATION.getCode());
            ServletOutputStream outputStream = response.getOutputStream();

            RespResult<String> respResult = RespResult.error(RespCode.UN_AUTHORIZATION);

            outputStream.write(objectMapper.writeValueAsString(respResult).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(RespCode.UN_FORBIDDEN.getCode());
            ServletOutputStream outputStream = response.getOutputStream();

            RespResult<String> respResult = RespResult.error(RespCode.UN_FORBIDDEN);

            outputStream.write(objectMapper.writeValueAsString(respResult).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

