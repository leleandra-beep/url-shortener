package com.example.urlshortener;

import com.example.urlshortener.config.AppConfig;
import com.example.urlshortener.core.*;
import com.example.urlshortener.infra.InMemoryLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceIntegrationTest {
    private LinkService service;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    void setUp() {
        service = new LinkServiceImpl(new InMemoryLinkRepository(), new AppConfig());
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
    }

    @Test
    void fullScenarioTwoUsersSameUrl() {
        String url = "https://www.baeldung.com/java-9-http-client";
        ShortLink a = service.createShortLink(url, alice, 3);
        ShortLink b = service.createShortLink(url, bob, 3);

        assertNotEquals(a.getShortCode(), b.getShortCode());
        assertEquals(2, service.getUserLinks(alice).size());
        assertEquals(2, service.getUserLinks(bob).size());
    }

    @Test
    void aliceCannotSeeBobLinksInList() {
        service.createShortLink("https://a.com", alice);
        service.createShortLink("https://b.com", bob);
        assertEquals(1, service.getUserLinks(alice).size());
        assertEquals(1, service.getUserLinks(bob).size());
    }

    @Test
    void limitExhaustedNotification() {
        ShortLink link = service.createShortLink("https://test.com", alice, 2);
        service.resolve(link.getShortCode());
        service.resolve(link.getShortCode());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.resolve(link.getShortCode()));
        assertTrue(ex.getMessage().contains("Лимит"));
    }

    @Test
    void cleanupRemovesOnlyExpired() throws Exception {
        ShortLink shortLived = service.createShortLink("https://short.com", alice, 10);
        // Принудительно делаем ссылку просроченной
        shortLived.setExpiresAt(java.time.LocalDateTime.now().minusHours(1));
        ShortLink valid = service.createShortLink("https://valid.com", alice, 10);

        service.cleanupExpired();

        assertThrows(IllegalArgumentException.class,
                () -> service.resolve(shortLived.getShortCode()));
        assertDoesNotThrow(() -> service.resolve(valid.getShortCode()));
    }

    @Test
    void multipleLinksPerUserSupported() {
        service.createShortLink("https://one.com", alice);
        service.createShortLink("https://two.com", alice);
        service.createShortLink("https://three.com", alice);
        assertEquals(3, service.getUserLinks(alice).size());
    }
}