package com.example.urlshortener.core;

import java.util.UUID;

public class UserContext {
    private UUID userId;

    public UserContext() {
        this.userId = UUID.randomUUID();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
