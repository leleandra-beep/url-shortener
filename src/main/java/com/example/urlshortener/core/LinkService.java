package com.example.urlshortener.core;

import java.util.List;
import java.util.UUID;

public interface LinkService {
    ShortLink createShortLink(String originalUrl, UUID ownerId, int maxVisits);
    ShortLink createShortLink(String originalUrl, UUID ownerId);
    ShortLink resolve(String shortCode);
    List<ShortLink> getUserLinks(UUID ownerId);
    void updateMaxVisits(String shortCode, UUID requesterId, int newMaxVisits);
    void deleteLink(String shortCode, UUID requesterId);
    void cleanupExpired();
}