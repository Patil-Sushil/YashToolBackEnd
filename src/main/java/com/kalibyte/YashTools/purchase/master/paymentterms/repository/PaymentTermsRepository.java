package com.kalibyte.YashTools.purchase.master.paymentterms.repository;

import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTermsRepository extends JpaRepository<PaymentTerms, UUID> {
    Optional<PaymentTerms> findByCode(String code);
}
