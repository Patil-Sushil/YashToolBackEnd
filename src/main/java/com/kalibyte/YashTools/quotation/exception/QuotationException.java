package com.kalibyte.YashTools.quotation.exception;

import com.kalibyte.YashTools.common.exception.BusinessException;

public class QuotationException extends BusinessException {
    public QuotationException(String message) { super(message); }
    public QuotationException(String message, Throwable cause) { super(message, cause); }
}