package com.kalibyte.YashTools.purchase.transaction.vendorpayment.validator;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentStatus;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository.VendorPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VendorPaymentValidator {

    private final VendorPaymentRepository repository;

    public void validateCreate(UUID companyId, VendorPaymentRequest request) {
        validateReferenceNumber(companyId, request.getPaymentReferenceNumber(), null);
    }

    public void validateUpdate(UUID companyId, VendorPaymentRequest request, UUID paymentId) {
        validateReferenceNumber(companyId, request.getPaymentReferenceNumber(), paymentId);
    }

    private void validateReferenceNumber(UUID companyId, String refNum, UUID paymentId) {
        if (refNum != null && !refNum.trim().isEmpty()) {
            boolean exists;
            if (paymentId == null) {
                exists = repository.existsByCompanyIdAndPaymentReferenceNumber(companyId, refNum.trim());
            } else {
                exists = repository.existsByCompanyIdAndPaymentReferenceNumberAndIdNot(companyId, refNum.trim(), paymentId);
            }
            if (exists) {
                throw new BusinessException("Payment reference number '" + refNum + "' already exists in this company.");
            }
        }
    }

    public void validateInvoiceForPayment(PurchaseInvoice invoice, BigDecimal paymentAmount) {
        // Validation 1: Do not allow payment against Cancelled Purchase Invoice
        if (invoice.getStatus() == PurchaseInvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot record payment against Cancelled Purchase Invoice: " + invoice.getInvoiceNumber());
        }

        // Validation 2: Do not allow payment against Fully Paid Invoice
        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cannot record payment against Fully Paid Purchase Invoice: " + invoice.getInvoiceNumber());
        }

        // Validation 3: Do not allow payment greater than invoice outstanding.
        if (paymentAmount.compareTo(invoice.getOutstandingAmount()) > 0) {
            throw new BusinessException(String.format("Payment amount %s exceeds outstanding amount %s for Purchase Invoice %s",
                    paymentAmount.stripTrailingZeros().toPlainString(),
                    invoice.getOutstandingAmount().stripTrailingZeros().toPlainString(),
                    invoice.getInvoiceNumber()));
        }
    }
}
