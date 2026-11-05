package com.example.urlshortener.infra;

import com.example.urlshortener.core.ShortLink;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, ShortLink> store = new ConcurrentHashMap<>();

    @Override
    public void save(ShortLink link) {
        store.put(link.getShortCode(), link);
    }

    @Override
    public Optional<ShortLink> findByShortCode(String shortCode) {
        return Optional.ofNullable(store.get(shortCode));
    }

    @Override
    public List<ShortLink> findByOwnerId(UUID ownerId) {
        return store.values().stream()
                .filter(l -> l.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByShortCode(String shortCode) {
        store.remove(shortCode);
    }

    @Override
    public List<ShortLink> findAll() {
        return new ArrayList<>(store.values());
    }
}