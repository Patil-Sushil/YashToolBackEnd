package com.kalibyte.YashTools.email.exception;

public class EmailSendException extends EmailException {
    public EmailSendException(String message) { super(message); }
    public EmailSendException(String message, Throwable cause) { super(message, cause); }
}