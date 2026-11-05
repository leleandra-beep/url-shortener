package com.example.urlshortener.infra;

import com.example.urlshortener.core.LinkService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanupScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(LinkService linkService, long intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                linkService.cleanupExpired();
            } catch (Exception e) {
                System.err.println("Cleanup error: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
