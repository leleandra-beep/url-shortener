package com.example.urlshortener.infra;

import com.example.urlshortener.core.ShortLink;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    void save(ShortLink link);
    Optional<ShortLink> findByShortCode(String shortCode);
    List<ShortLink> findByOwnerId(java.util.UUID ownerId);
    void deleteByShortCode(String shortCode);
    List<ShortLink> findAll();
}
