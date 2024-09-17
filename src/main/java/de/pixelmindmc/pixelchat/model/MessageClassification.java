package de.pixelmindmc.pixelchat.model;

public record MessageClassification(boolean block, String reason, Action action) {

    public enum Action {
        KICK,
        BAN,
        NONE
    }
}
