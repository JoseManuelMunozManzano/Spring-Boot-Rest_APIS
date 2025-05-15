package com.jmunoz.todos.service;

import com.jmunoz.todos.request.AuthenticationRequest;
import com.jmunoz.todos.request.RegisterRequest;
import com.jmunoz.todos.response.AuthenticationResponse;

public interface AuthenticationService {
    void register(RegisterRequest input) throws Exception;
    AuthenticationResponse login(AuthenticationRequest request);
}
