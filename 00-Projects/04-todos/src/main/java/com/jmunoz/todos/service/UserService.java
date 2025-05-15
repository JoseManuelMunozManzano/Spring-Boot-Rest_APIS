package com.jmunoz.todos.service;

import com.jmunoz.todos.request.PasswordUpdateRequest;
import com.jmunoz.todos.response.UserResponse;

import java.nio.file.AccessDeniedException;

public interface UserService {
    UserResponse getUserInfo();
    void deleteUser();
    void updatePassword(PasswordUpdateRequest passwordUpdateRequest);
}
