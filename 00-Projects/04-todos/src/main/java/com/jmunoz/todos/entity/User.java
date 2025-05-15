package com.jmunoz.todos.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

// Si la tabla no existe en BBDD, al ejecutarse el proyecto se creará.
// Implementa UserDetails para que se configure la autenticación usando Spring Security.

@Table(name = "users")
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // Dos users no pueden tener el mismo email.
    // Longitud máxima de 100 caracteres.
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Con @CreationTimestamp el valor se añade directamente a la BBDD.
    // No puede cambiarse el valor una vez creado.
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    // Con @UpdateTimestamp el valor se añade directamente a la BBDD.
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    // Trabajando con la entity Authority.
    // Desde aquí se indica que debe crearse la tabla authorities al crearse la tabla users.
    // Se indica EAGER, para que cuando se traiga un user, traigamos todas sus autorizaciones.
    // Se indica @CollectionTable porque es una lista y podría tener varios items y hacemos
    // que el campo user_authorities = user_id
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    private List<Authority> authorities;

    // Un user puede tener muchos todos.
    // La relación está manejada por el campo owner en la entity Todo.
    // CascadeType.ALL indica que todas las operaciones de create, update y delete sobre user irá
    // en cascada a los todos.
    // orphanRemoval=true indica que, si eliminamos un users (con todos su to-dos) y hay un to-do
    // que no está asociado a ningún user, este se elimina de BBDD.
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos;

    public User() {}

    // La entity Authority implementa GrantedAuthority!!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
