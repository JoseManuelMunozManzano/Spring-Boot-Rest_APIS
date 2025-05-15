package com.jmunoz.todos.entity;

import jakarta.persistence.Embeddable;
import org.springframework.security.core.GrantedAuthority;

// GrantedAuthority es una interface principal de Spring Security.
// Representa una autorización y/o permiso o rol de un user.
// GrantedAuthority se usa en la entity User.
// @Embeddable significa que este objeto (authority) es parte de otro objeto (user).
// Va a ser una tabla que conecta al user con su authority (roles).

@Embeddable
public class Authority implements GrantedAuthority {

    private String authority;

    public Authority() {}

    public Authority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
