package com.example.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final long ttlHours;
    private final int defaultMaxVisits;
    private final long cleanupIntervalSeconds;

    public AppConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
        this.ttlHours = Long.parseLong(props.getProperty("link.ttl.hours", "24"));
        this.defaultMaxVisits = Integer.parseInt(
                props.getProperty("link.max.visits.default", "100"));
        this.cleanupIntervalSeconds = Long.parseLong(
                props.getProperty("cleanup.interval.seconds", "60"));
    }

    public long getTtlHours() { return ttlHours; }
    public int getDefaultMaxVisits() { return defaultMaxVisits; }
    public long getCleanupIntervalSeconds() { return cleanupIntervalSeconds; }
}