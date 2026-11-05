package com.example.urlshortener.core;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private final String shortCode;
    private final String originalUrl;
    private final UUID ownerId;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int maxVisits;
    private int visitCount;

    public ShortLink(String shortCode, String originalUrl, UUID ownerId,
                     LocalDateTime createdAt, LocalDateTime expiresAt, int maxVisits) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxVisits = maxVisits;
        this.visitCount = 0;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isLimitReached() {
        return visitCount >= maxVisits;
    }

    public boolean isAvailable() {
        return !isExpired() && !isLimitReached();
    }

    public void recordVisit() {
        if (!isAvailable()) {
            throw new IllegalStateException("Link is not available");
        }
        visitCount++;
    }

    // Getters and setters
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public UUID getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getMaxVisits() { return maxVisits; }
    public void setMaxVisits(int maxVisits) { this.maxVisits = maxVisits; }
    public int getVisitCount() { return visitCount; }
}