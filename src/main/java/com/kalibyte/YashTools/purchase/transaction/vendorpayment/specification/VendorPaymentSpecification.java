package com.kalibyte.YashTools.purchase.transaction.vendorpayment.specification;

import com.kalibyte.YashTools.purchase.shared.enums.PaymentMethod;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPayment;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class VendorPaymentSpecification {

    private VendorPaymentSpecification() {}

    public static Specification<VendorPayment> hasVendor(UUID vendorId) {
        return (root, query, cb) -> vendorId == null ? null : cb.equal(root.get("vendor").get("id"), vendorId);
    }

    public static Specification<VendorPayment> hasPaymentMethod(PaymentMethod paymentMethod) {
        return (root, query, cb) -> paymentMethod == null ? null : cb.equal(root.get("paymentMethod"), paymentMethod);
    }

    public static Specification<VendorPayment> hasPaymentDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.equal(root.get("paymentDate"), date);
    }

    public static Specification<VendorPayment> hasPaymentNumber(String paymentNumber) {
        return (root, query, cb) -> {
            if (paymentNumber == null || paymentNumber.trim().isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("paymentNumber")), "%" + paymentNumber.trim().toLowerCase() + "%");
        };
    }
}
