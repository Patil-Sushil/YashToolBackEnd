package com.kalibyte.YashTools.email.exception;

import com.kalibyte.YashTools.common.exception.BusinessException;

public class EmailException extends BusinessException {
    public EmailException(String message) { super(message); }
    public EmailException(String message, Throwable cause) { super(message, cause); }
}