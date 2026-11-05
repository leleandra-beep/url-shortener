package com.example.urlshortener;

import com.example.urlshortener.config.AppConfig;
import com.example.urlshortener.core.*;
import com.example.urlshortener.infra.*;
import com.example.urlshortener.ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        LinkRepository repository = new InMemoryLinkRepository();
        LinkService linkService = new LinkServiceImpl(repository, config);

        CleanupScheduler scheduler = new CleanupScheduler();
        scheduler.start(linkService, config.getCleanupIntervalSeconds());

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        UserContext userContext = new UserContext();
        ConsoleUI ui = new ConsoleUI(linkService, userContext);
        ui.run();
    }
}
