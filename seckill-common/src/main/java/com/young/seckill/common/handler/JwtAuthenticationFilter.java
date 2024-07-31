package com.young.seckill.common.handler;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final String USER_ID              = "userId";

    private final RedisTemplate<String, Object> redisTemplate;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, RedisTemplate<String, Object> redisTemplate) {
        super(authenticationManager);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = servletRequest.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        } else {
            String token = authorization.substring(AUTHORIZATION_PREFIX.length());

            Long userId = JwtProvider.getUserIdFromToken(token);

            if (redisTemplate.opsForValue().get(SeckillConstants.getKey(SeckillConstants.USER_KEY, userId)) != null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, null);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(servletRequest));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(servletRequest);
                requestWrapper.setAttribute(USER_ID, userId);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
