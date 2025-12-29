package com.delivery.auth.converter;

import com.delivery.auth.dto.RegisterRequest;
import com.delivery.auth.dto.UserRequest;
import com.delivery.auth.dto.UserResponse;
import com.delivery.db.entity.user.User;
import com.delivery.db.entity.user.Grade;

public class UserConverter {

    public static UserRequest toRequest(User user) {
        if (user == null) return null;
        return UserRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .grade(user.getGrade())
                .build();
    }

    public static UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .grade(user.getGrade())
                .build();
    }

    public static User toEntity(RegisterRequest request, String encodedPassword) {
        if (request == null) return null;
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .role(request.getRole() == null ? "ROLE_USER" : request.getRole())
                .grade(Grade.BRONZE)
                .build();
    }
}
