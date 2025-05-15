package com.jmunoz.todos.config;

import com.jmunoz.todos.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter se asegura de que este filtro se ejecuta una vez, cada vez que hay una petición HTTP.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Indicamos @Lazy para retrasar la creación y/o inyección del bean hasta que sea realmente necesario.
    // Cuando Spring se ejecuta, crea un proxy de UserDetailsService y solo lo crea cuando se usa por primera vez.
    // La principal razón por la que implementamos @Lazy es para corregir dependencias circulares.
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // Indicamos @NonNull nos permite estar seguros de que esos objetos tendrán valores y no serán null.
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Siempre debería venir la palabra Bearer al principio.
        // Si no, va al siguiente filtro, pero sin usuario.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos la palabra Bearer.
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // Solo procedemos si obtenemos un email válido y no Authentication es lo que hay configurado en SecurityContextHolder.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carga los detalles del usuario de la BBDD donde se almacena user usando el email, ya que en userDetailsService Username es email.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Creamos un nuevo token de autenticación
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Creamos un user que puede usar Spring Security.
                // Las credentials son null porque usamos JWT.
                // userDetails.getAuthorities() nos sirve para obtener todos los permisos del user.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Configuramos a ese usuario en Spring Security.
                // Una vez que JWT pasa y se autentica en nuestro código, podemos agrupar ese user y usarlo muy fácilmente en la aplicación.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Que vaya al siguiente filtro.
        filterChain.doFilter(request, response);
    }
}
