package com.kalibyte.YashTools.sales.invoice.email;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceEmailHandlerImpl implements SalesInvoiceEmailHandler {

    private final SalesInvoiceEmailComposer composer;
    private final EmailService emailService;

    @Override
    public UUID sendInvoiceEmail(SalesInvoiceResponse invoice) {
        return sendInvoiceEmail(invoice, null, List.of());
    }

    @Override
    public UUID sendInvoiceEmail(SalesInvoiceResponse invoice, byte[] pdf) {
        return sendInvoiceEmail(invoice, pdf, List.of());
    }

    @Override
    public UUID sendInvoiceEmail(SalesInvoiceResponse invoice, byte[] pdf, List<String> cc) {
        EmailMessage message = composer.compose(invoice, pdf, cc);
        EmailResult result = emailService.send(message);
        log.info("Sales Invoice email dispatched: success={} logId={} subject='{}'",
                result.isSuccess(), result.getEmailLogId(), invoice.getInvoiceNo());
        return result.getEmailLogId();
    }
}
