package com.example.authService.Security;

import com.example.authService.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtCore jwtCore;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
           String header = request.getHeader("Authorization");
           if(header != null && header.startsWith("Bearer ")){
               String token= header.substring(7);
               if(jwtCore.validateToken(token)){
                   String username= jwtCore.getUsernameFromToken(token);
                   UserDetails userDetails= customUserDetailsService.loadUserByUsername(username);

                   UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                         userDetails,null,userDetails.getAuthorities()
                   );

                   authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(authenticationToken);

               }
           }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context: {}", e);
        }

        filterChain.doFilter(request,response);
    }
}
