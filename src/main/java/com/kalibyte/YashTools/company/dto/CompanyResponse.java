package com.kalibyte.YashTools.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private UUID id;
    private String code;
    private String name;
    private String gstNumber;
    private String bankName;
    private String bankAccountNo;
    private String bankIfsc;
    private String bankBranch;
    private String address;
    private String logoUrl;
}
