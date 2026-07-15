package com.kalibyte.YashTools.purchase.master.vendor.repository;

import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByVendorName(String vendorName);
}
