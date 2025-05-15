package com.jmunoz.todos.service;

import com.jmunoz.todos.entity.Authority;
import com.jmunoz.todos.entity.User;
import com.jmunoz.todos.repository.UserRepository;
import com.jmunoz.todos.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

// Solo los admin pueden acceder a estos servicios.
// Esto se configura en SecurityConfig y por eso aquí no se comprueba si el usuario es un admin.
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = false)
    public List<UserResponse> getAllUsers() {
        // Convertimos un resultado Iterable a un stream.
        // Luego le aplicamos el map para transformar cada User entity a UserResponse.
        // Y por último lo pasamos a una lista.
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(this::convertToUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse promoteToAdmin(long userId) {
        // Nos aseguramos que el user existe y que no es admin.
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty() || user.get().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist or already an admin");
        }

        // Sustituimos los roles que tuviera el user por estos (añadiendo el de admin).
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority("ROLE_EMPLOYEE"));
        authorities.add(new Authority("ROLE_ADMIN"));
        user.get().setAuthorities(authorities);

        // Actualizamos el user
        User savedUser = userRepository.save(user.get());

        // Transformarmos el user actualizado a UserResponse y lo devolvemos.
        return convertToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteNonAdminUser(long userId) {
        // Nos aseguramos que el user existe y que no es admin.
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty() || user.get().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist or already an admin");
        }

        userRepository.delete(user.get());
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getAuthorities().stream().map(auth -> (Authority) auth).toList()
        );
    }
}
