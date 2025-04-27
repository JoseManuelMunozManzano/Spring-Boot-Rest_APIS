package com.jmunoz.employees.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

// Configuración para usar tablas que reconoce Spring Security por defecto (users y authorities)
// Para que Spring no la tenga en cuenta, comentamos la anotación.

//@Configuration
public class SecurityConfigDefaultTables {

    // Añadiendo soporte para JDBC. La información de los usuarios/roles se obtiene de la BBDD.
    // Usamos tablas por defecto, users y authorities
    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    // Restricciones basadas en el rol del usuario y el endpoint al que se quiere acceder.
    // Los path que no aparezcan aquí, por defecto están protegidos y ningún usuario podrá acceder.
    // A Swagger tendrá acceso cualquier usuario.
    // A H2 tendrá acceso cualquier usuario.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.GET, "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/h2-console/**").permitAll()
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employees").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                );

        // Deshabilitar Basic Authentication por default (en el navegador) para poder gestionarlo en Swagger.
        http.httpBasic(httpBasicCustomizer -> httpBasicCustomizer.disable());

        // Usa HTTP Basic Authentication
        http.httpBasic(Customizer.withDefaults());

        // Deshabilita CSRF
        http.csrf(csrf -> csrf.disable());

        // Si falla, llama a la funcionalidad del méto-do authenticationEntryPoint()
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint()));

        // La consola H2 necesita frames para operar, así que en esta cabecera lo deshabilitamos
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    // Personalizamos lo que ocurre cuando se produce una excepción por falta de permisos de autorización.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {
            // Manda un estado 401 Unauthorized sin disparar el pop-up de basic authentication en el navegador.
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            // Elimina el header WWW-Authenticate, que también evita el pop-up en el navegador.
            response.setHeader("WWW-Authenticate", "");
            response.getWriter().write("{\"error\": \"Unauthorized access\"}");
        };
    }
}
