package com.kalibyte.YashTools.purchase.master.vendor.repository;

import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByVendorName(String vendorName);


    @org.springframework.data.jpa.repository.Query("SELECT v FROM Vendor v WHERE " +
            "LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.gstin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.pan) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.contactDetails) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.address) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<Vendor> searchVendors(
            @org.springframework.data.repository.query.Param("search") String search);

}
