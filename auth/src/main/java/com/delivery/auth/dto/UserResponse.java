package com.delivery.auth.dto;

import com.delivery.db.entity.user.Grade;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String role;
    private Grade grade;
}
