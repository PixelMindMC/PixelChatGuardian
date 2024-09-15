package de.pixelmindmc.pixelchat.utils;

public record MessageClassification(boolean block, String reason, Action action) {

    public enum Action {
        KICK,
        BAN,
        NONE
    }
}
