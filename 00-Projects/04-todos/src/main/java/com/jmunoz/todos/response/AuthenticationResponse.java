package com.jmunoz.todos.response;

// Esta response es similar al entity, pero eliminando cierta data que la aplicación frontend no necesita.
// En concreto eliminamos el password
public class AuthenticationResponse {
    private String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
