package com.jmunoz.todos.config;

import com.jmunoz.todos.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration es una anotación del core de Spring que indica que esta clase declara uno o más métodos bean.
// También indica a Spring que la clase debe tratarse como una fuente de definiciones de beans.
// Esta clase se procesará durante el inicio de la aplicación para generar las definiciones de los beans y
// las peticiones de servicio de los beans en tiempo de ejecución.
// @EnableWebSecurity es una anotación que habilita el soporte de seguridad web de Spring Security.
// Nos permite usar cadenas por defecto de filtros, login basado en formularios, autenticación HTTP básica y
// protege contra ataques comunes como CSRF, XSS y otros.
//
// Vamos a extender y definir un filtro de seguridad en esta clase, para configurar como trabajan juntos nuestros API
// endpoints, manejo de excepciones y filtros de autenticación.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserRepository userRepository, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager es una interface core de Spring Security.
    // Procesa peticiones de autenticación. Verifica las credenciales de usuario y crea una sesión autenticada
    // si las credenciales son válidas.
    // Como estamos usando autenticación basada en JWT en nuestra aplicación, AuthenticationManager validará
    // las credenciales de usuario durante el login.
    // Va a trabajar con UserDetailService usando PasswordEncoder (sabe usar el encoder password Bcrypt)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Punto de entrada de la autenticación. Si intentamos autenticar un user y se deniega y necesitamos
    // lanzar una excepción, aquí es donde se hace.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setHeader("WWW-Authenticate", "");
            response.getWriter().write("{\"error\": \"Unauthorized access\"}");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        // Peticiones permitidas para to-do el mundo.
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/docs").permitAll()
                        // Peticiones permitidas solo para admins.
                        // En BD el role es ROLE_ADMIN, pero Spring Boot Security añade automáticamente la palabra ROLE_
                        // de ahí que no haga falta indicarla.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Peticiones para las que el usuario debe estar autenticado.
                        .anyRequest().authenticated());

        http.csrf(csrf -> csrf.disable());

        http.exceptionHandling(exceptionHandling ->
                exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint()));

        // stateless, es decir, las peticiones no almacenan ningún tipo de cookie u otra cosa, y se
        // gestionan independientemente, es decir, todas las peticiones deben estar autenticadas.
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Antes de hacer cualquier cosa de Spring Security, hay que pasar el filtro de autenticación JWT.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
