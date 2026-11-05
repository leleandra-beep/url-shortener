package com.example.urlshortener.core;

import com.example.urlshortener.config.AppConfig;
import com.example.urlshortener.infra.LinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LinkServiceImpl implements LinkService {
    private final LinkRepository repository;
    private final AppConfig config;

    public LinkServiceImpl(LinkRepository repository, AppConfig config) {
        this.repository = repository;
        this.config = config;
    }

    @Override
    public ShortLink createShortLink(String originalUrl, UUID ownerId) {
        return createShortLink(originalUrl, ownerId, config.getDefaultMaxVisits());
    }

    @Override
    public ShortLink createShortLink(String originalUrl, UUID ownerId, int maxVisits) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        if (maxVisits <= 0) {
            throw new IllegalArgumentException("Max visits must be positive");
        }

        // Генерация уникального кода: UUID + ownerId hash → короткий код
        String shortCode;
        do {
            shortCode = generateCode(ownerId, originalUrl);
        } while (repository.findByShortCode(shortCode).isPresent());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(config.getTtlHours());
        ShortLink link = new ShortLink(shortCode, originalUrl, ownerId, now, expiresAt, maxVisits);
        repository.save(link);
        return link;
    }

    private String generateCode(UUID ownerId, String url) {
        // Комбинация ownerId и url гарантирует уникальность для разных пользователей
        String seed = ownerId.toString() + url + System.nanoTime();
        int hash = Math.abs(seed.hashCode());
        return "u" + Integer.toString(hash, 36).substring(0, 6);
    }

    @Override
    public ShortLink resolve(String shortCode) {
        ShortLink link = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found: " + shortCode));

        if (link.isExpired()) {
            repository.deleteByShortCode(shortCode);
            throw new IllegalStateException(
                    "Ссылка истекла (TTL) и была удалена. Создайте новую ссылку.");
        }
        if (link.isLimitReached()) {
            throw new IllegalStateException(
                    "Лимит переходов исчерпан (" + link.getVisitCount() + "/" + link.getMaxVisits()
                            + "). Создайте новую ссылку.");
        }

        link.recordVisit();
        repository.save(link);
        return link;
    }

    @Override
    public List<ShortLink> getUserLinks(UUID ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    @Override
    public void updateMaxVisits(String shortCode, UUID requesterId, int newMaxVisits) {
        ShortLink link = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        if (!link.getOwnerId().equals(requesterId)) {
            throw new SecurityException("Только владелец может редактировать ссылку");
        }
        if (newMaxVisits <= 0) {
            throw new IllegalArgumentException("Max visits must be positive");
        }
        link.setMaxVisits(newMaxVisits);
        repository.save(link);
    }

    @Override
    public void deleteLink(String shortCode, UUID requesterId) {
        ShortLink link = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        if (!link.getOwnerId().equals(requesterId)) {
            throw new SecurityException("Только владелец может удалить ссылку");
        }
        repository.deleteByShortCode(shortCode);
    }

    @Override
    public void cleanupExpired() {
        repository.findAll().stream()
                .filter(ShortLink::isExpired)
                .forEach(l -> repository.deleteByShortCode(l.getShortCode()));
    }
}
