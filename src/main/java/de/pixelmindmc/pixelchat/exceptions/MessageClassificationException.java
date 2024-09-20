package de.pixelmindmc.pixelchat.exceptions;

public class MessageClassificationException extends Exception {
    public MessageClassificationException(String message) {
        super(message);
    }

    public MessageClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
