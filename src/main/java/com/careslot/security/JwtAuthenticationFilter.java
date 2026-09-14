package com.careslot.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter  extends OncePerRequestFilter {

    private final JwtServices jwtServices;


    public JwtAuthenticationFilter(JwtServices jwtServices) {
        this.jwtServices = jwtServices;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader=request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader==null||!authHeader.startsWith("Bearer")){
            filterChain.doFilter(request,response);
            return;
        }

        jwt=authHeader.substring(7);

        try {
            userEmail=jwtServices.extractUsername(jwt);

            if(userEmail!=null&& SecurityContextHolder.getContext().getAuthentication()==null){
                String userId=jwtServices.extractClaim(jwt,claims -> claims.get("userId",String.class));
                String role = jwtServices.extractClaim(jwt,claims -> claims.get("role", String.class));

                if (jwtServices.isTokenValid(jwt,userEmail)){
                    var authorities= Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_"+role)
                    );
                    var authToken=new UsernamePasswordAuthenticationToken(
                            userId,null,authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.debug("JWT validation failed: " + e.getMessage());
        }
        filterChain.doFilter(request,response);
    }
}
