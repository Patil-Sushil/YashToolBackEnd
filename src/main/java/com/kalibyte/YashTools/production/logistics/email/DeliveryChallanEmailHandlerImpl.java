package com.kalibyte.YashTools.production.logistics.email;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryChallanEmailHandlerImpl implements DeliveryChallanEmailHandler {

    private final DeliveryChallanEmailComposer composer;
    private final EmailService emailService;

    @Override
    public UUID sendChallanEmail(DeliveryChallanResponse challan) {
        return sendChallanEmail(challan, null, List.of());
    }

    @Override
    public UUID sendChallanEmail(DeliveryChallanResponse challan, byte[] pdf) {
        return sendChallanEmail(challan, pdf, List.of());
    }

    @Override
    public UUID sendChallanEmail(DeliveryChallanResponse challan, byte[] pdf, List<String> cc) {
        EmailMessage message = composer.compose(challan, pdf, cc);
        EmailResult result = emailService.send(message);
        log.info("Delivery Challan email dispatched: success={} logId={} subject='{}'",
                result.isSuccess(), result.getEmailLogId(), challan.getChallanNo());
        return result.getEmailLogId();
    }
}
