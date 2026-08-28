package com.fidecode.biteflow.application.auth.dto;

import java.util.List;

public record UpdateUserRequest(String name, String password, String role, List<String> permissions) {
}
