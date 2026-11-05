package com.example.urlshortener.ui;

import com.example.urlshortener.core.*;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final LinkService linkService;
    private final UserContext userContext;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(LinkService linkService, UserContext userContext) {
        this.linkService = linkService;
        this.userContext = userContext;
    }

    public void run() {
        printWelcome();
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "shorten" -> handleShorten(parts);
                    case "open" -> handleOpen(parts);
                    case "list" -> handleList();
                    case "update" -> handleUpdate(parts);
                    case "delete" -> handleDelete(parts);
                    case "whoami" -> System.out.println("Ваш UUID: " + userContext.getUserId());
                    case "exit" -> { System.out.println("До свидания!"); return; }
                    default -> System.out.println("Неизвестная команда. Введите 'help'.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Доступ запрещён: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
    }

    private void printWelcome() {
        System.out.println("=== Сервис сокращения ссылок ===");
        System.out.println("Ваш UUID: " + userContext.getUserId());
        System.out.println("Введите 'help' для списка команд.");
    }

    private void printHelp() {
        System.out.println("""
            Команды:
              shorten <url> [maxVisits]  — создать короткую ссылку
              open <shortCode>           — перейти по короткой ссылке (открыть браузер)
              list                       — показать все ваши ссылки
              update <shortCode> <n>     — изменить лимит переходов
              delete <shortCode>         — удалить ссылку
              whoami                     — показать ваш UUID
              exit                       — выход
            """);
    }

    private void handleShorten(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Использование: shorten <url> [maxVisits]");
            return;
        }
        String url = parts[1];
        int maxVisits = -1;
        if (parts.length >= 3) {
            try {
                maxVisits = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("maxVisits должен быть числом");
                return;
            }
        }
        ShortLink link = (maxVisits > 0)
                ? linkService.createShortLink(url, userContext.getUserId(), maxVisits)
                : linkService.createShortLink(url, userContext.getUserId());
        System.out.println("Создана ссылка: " + link.getShortCode());
        System.out.println("Оригинал: " + link.getOriginalUrl());
        System.out.println("TTL до: " + link.getExpiresAt());
        System.out.println("Лимит: " + link.getMaxVisits());
    }

    private void handleOpen(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Использование: open <shortCode>");
            return;
        }
        ShortLink link = linkService.resolve(parts[1]);
        System.out.println("Переход: " + link.getOriginalUrl());
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
        } else {
            System.out.println("(Браузер не поддерживается, откройте вручную)");
        }
    }

    private void handleList() {
        List<ShortLink> links = linkService.getUserLinks(userContext.getUserId());
        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок.");
            return;
        }
        System.out.printf("%-10s %-10s %-6s %-20s %s%n",
                "Код", "Переходы", "Лимит", "Истекает", "URL");
        for (ShortLink l : links) {
            System.out.printf("%-10s %-10d %-6d %-20s %s%n",
                    l.getShortCode(), l.getVisitCount(), l.getMaxVisits(),
                    l.getExpiresAt(), l.getOriginalUrl());
        }
    }

    private void handleUpdate(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Использование: update <shortCode> <newMaxVisits>");
            return;
        }
        int newMax = Integer.parseInt(parts[2]);
        linkService.updateMaxVisits(parts[1], userContext.getUserId(), newMax);
        System.out.println("Лимит обновлён.");
    }

    private void handleDelete(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Использование: delete <shortCode>");
            return;
        }
        linkService.deleteLink(parts[1], userContext.getUserId());
        System.out.println("Ссылка удалена.");
    }
}