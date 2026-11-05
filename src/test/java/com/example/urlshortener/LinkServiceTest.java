package com.example.urlshortener;

import com.example.urlshortener.config.AppConfig;
import com.example.urlshortener.core.*;
import com.example.urlshortener.infra.InMemoryLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {
    private LinkService service;
    private UUID owner;

    @BeforeEach
    void setUp() {
        AppConfig config = new AppConfig();
        service = new LinkServiceImpl(new InMemoryLinkRepository(), config);
        owner = UUID.randomUUID();
    }

    @Test
    void createShortLinkReturnsLink() {
        ShortLink link = service.createShortLink("https://example.com/page", owner);
        assertNotNull(link.getShortCode());
        assertEquals("https://example.com/page", link.getOriginalUrl());
        assertEquals(owner, link.getOwnerId());
    }

    @Test
    void invalidUrlThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createShortLink("not-a-url", owner));
    }

    @Test
    void emptyUrlThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createShortLink("", owner));
    }

    @Test
    void differentUsersGetDifferentCodes() {
        UUID owner2 = UUID.randomUUID();
        String url = "https://example.com/same";
        ShortLink l1 = service.createShortLink(url, owner);
        ShortLink l2 = service.createShortLink(url, owner2);
        assertNotEquals(l1.getShortCode(), l2.getShortCode());
    }

    @Test
    void visitCountIncrements() {
        ShortLink link = service.createShortLink("https://example.com", owner, 5);
        service.resolve(link.getShortCode());
        service.resolve(link.getShortCode());
        assertEquals(2, link.getVisitCount());
    }

    @Test
    void limitReachedBlocksAccess() {
        ShortLink link = service.createShortLink("https://example.com", owner, 1);
        service.resolve(link.getShortCode());
        assertThrows(IllegalStateException.class,
                () -> service.resolve(link.getShortCode()));
    }

    @Test
    void onlyOwnerCanUpdate() {
        ShortLink link = service.createShortLink("https://example.com", owner);
        UUID stranger = UUID.randomUUID();
        assertThrows(SecurityException.class,
                () -> service.updateMaxVisits(link.getShortCode(), stranger, 10));
    }

    @Test
    void onlyOwnerCanDelete() {
        ShortLink link = service.createShortLink("https://example.com", owner);
        UUID stranger = UUID.randomUUID();
        assertThrows(SecurityException.class,
                () -> service.deleteLink(link.getShortCode(), stranger));
    }

    @Test
    void ownerCanUpdateLimit() {
        ShortLink link = service.createShortLink("https://example.com", owner, 5);
        service.updateMaxVisits(link.getShortCode(), owner, 10);
        assertEquals(10, link.getMaxVisits());
    }

    @Test
    void ownerCanDelete() {
        ShortLink link = service.createShortLink("https://example.com", owner);
        service.deleteLink(link.getShortCode(), owner);
        assertThrows(IllegalArgumentException.class,
                () -> service.resolve(link.getShortCode()));
    }
}
