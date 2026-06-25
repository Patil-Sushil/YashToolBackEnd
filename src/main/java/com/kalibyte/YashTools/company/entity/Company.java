package com.kalibyte.YashTools.company.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // 'YT', 'SW'

    @Column(nullable = false)
    private String name; // 'Yash Tools', 'Swara Enterprises'

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;


    private String bankIfsc;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "logo_url")
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
