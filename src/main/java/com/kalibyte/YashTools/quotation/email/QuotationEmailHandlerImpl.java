package com.kalibyte.YashTools.quotation.email;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationEmailHandlerImpl implements QuotationEmailHandler {

    private final QuotationEmailComposer composer;
    private final EmailService emailService;

    @Override
    public EmailResult sendQuotationEmail(QuotationResponse q) {
        return sendQuotationEmail(q, null, List.of());
    }

    @Override
    public EmailResult sendQuotationEmail(QuotationResponse q, byte[] pdf) {
        return sendQuotationEmail(q, pdf, List.of());
    }

    @Override
    public EmailResult sendQuotationEmail(QuotationResponse q, byte[] pdf, List<String> cc) {
        EmailMessage message = composer.compose(q, pdf, cc);
        EmailResult result = emailService.send(message);
        log.info("Quotation email dispatched: success={} logId={} subject='{}'",
                result.isSuccess(), result.getEmailLogId(), q.getQuotationNo());
        return result;
    }
}