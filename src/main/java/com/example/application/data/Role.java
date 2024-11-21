package com.example.application.data;

public enum Role {
    ROLE_STUDENT,
    ROLE_LECTURER,
    ROLE_ADMIN;

    @Override
    public String toString() {
        return name();
    }
}