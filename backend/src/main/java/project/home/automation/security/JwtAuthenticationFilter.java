package project.home.automation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.home.automation.service.CustomUserDetailsService;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        // Check if the token is provided and has the "Bearer" prefix (case-insensitive)
        if (token != null && token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7);  // Remove "Bearer " prefix
            String email = jwtUtil.extractEmail(token);
            // If username exists and no authentication is set in the context
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from the custom service
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                // Check if the token is valid
                if (jwtUtil.isTokenValid(token)) {
                    // Create authentication token and set it in the SecurityContext
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }
}


// Fire base code
//package project.home.automation.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import project.home.automation.service.CustomUserDetailsService;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//    private final CustomUserDetailsService customUserDetailsService;
//
//    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
//        this.jwtUtil = jwtUtil;
//        this.customUserDetailsService = customUserDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String token = request.getHeader("Authorization");
//
//        // Check if the token is provided and has the "Bearer" prefix (case-insensitive)
//        if (token != null && token.toLowerCase().startsWith("bearer ")) {
//            token = token.substring(7);  // Remove "Bearer " prefix
//
//            String email = jwtUtil.extractEmail(token);
//
//            // If username exists and no authentication is set in the context
//            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                // Load user details from the custom service
//                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
//
//                // Check if the token is valid
//                if (jwtUtil.isTokenValid(token)) {
//                    // Create authentication token and set it in the SecurityContext
//                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//                }
//            }
//        }
//
//        // Continue with the next filter in the chain
//        filterChain.doFilter(request, response);
//    }
//}