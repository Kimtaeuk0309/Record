package me.taeuk.record.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    // 인증 제외 경로 - startsWith로 부분 일치 허용
    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/js/",
            "/css/",
            "/images/",
            "/static/"
    );

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // 인증 제외 경로 체크
        boolean skipFilter = EXCLUDE_URLS.stream().anyMatch(path::startsWith);
        if (skipFilter) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = parseJwt(request);
            if (!StringUtils.hasText(token)) {
                logger.debug("Authorization 헤더에 토큰이 없습니다. 경로: {}", path);
            } else if (!jwtProvider.validateToken(token)) {
                logger.debug("JWT 토큰 검증 실패: {}", token);
            } else {
                String username = jwtProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("인증 성공 - 사용자: {}", username);
            }

        } catch (Exception e) {
            logger.error("JWT 인증 도중 예외 발생: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
