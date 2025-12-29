package com.delivery.domain.user;


import com.delivery.domain.user.Grade;
import com.delivery.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class User {
    private UUID id;
    private String email;
    private String name;
    private Role role;
    private Grade grade;
    private boolean active;

    public void rename(String name) {
        this.name = name;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void promoteGrade() {
        switch (this.grade) {
            case BRONZE -> this.grade = Grade.SILVER;
            case SILVER -> this.grade = Grade.GOLD;
            case GOLD -> { /* max grade */ }
        }
    }

    public void demoteGrade() {
        switch (this.grade) {
            case GOLD -> this.grade = Grade.SILVER;
            case SILVER -> this.grade = Grade.BRONZE;
            case BRONZE -> { /* min grade */ }
        }
    }

    public void grantAdmin() {
        this.role = Role.ROLE_ADMIN;
    }

    public void grantUser() {
        this.role = Role.ROLE_USER;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isAdmin() {
        return this.role == Role.ROLE_ADMIN;
    }

    public boolean isActive() {
        return this.active;
    }
}
