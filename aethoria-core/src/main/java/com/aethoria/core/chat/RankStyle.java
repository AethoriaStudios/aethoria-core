package com.aethoria.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public record RankStyle(String permission, Component prefix) {
    public static final Component CHAT_SEPARATOR = Component.text(" ", NamedTextColor.WHITE);
    public static final Component AETHORIAN_PREFIX = Component.text()
        .append(Component.text("[", NamedTextColor.RED))
        .append(Component.text("ADMIN", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("] ", NamedTextColor.RED))
        .build();

    public static RankStyle aethorian() {
        return new RankStyle("group.aethorian", AETHORIAN_PREFIX);
    }

    public static RankStyle vipd() {
        return new RankStyle(
            "group.vipd",
            Component.text("[VIPD] ", NamedTextColor.GOLD, TextDecoration.BOLD)
        );
    }

    public static RankStyle vipc() {
        return new RankStyle(
            "group.vipc",
            Component.text("[VIPC] ", NamedTextColor.AQUA, TextDecoration.BOLD)
        );
    }

    public static RankStyle vipb() {
        return new RankStyle(
            "group.vipb",
            Component.text("[VIPB] ", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
        );
    }

    public static RankStyle vipa() {
        return new RankStyle(
            "group.vipa",
            Component.text("[VIPA] ", NamedTextColor.GREEN, TextDecoration.BOLD)
        );
    }

    public static RankStyle defaultStyle() {
        return new RankStyle("", Component.empty());
    }
}
