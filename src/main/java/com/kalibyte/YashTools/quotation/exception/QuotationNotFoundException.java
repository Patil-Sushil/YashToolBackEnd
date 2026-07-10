package com.kalibyte.YashTools.quotation.exception;

public class QuotationNotFoundException extends QuotationException {
    public QuotationNotFoundException(String id) { super("Quotation not found: " + id); }
}