package com.kalibyte.YashTools.master.coating.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoatingResponse {
    private Long id;
    private String name;
    private String rate;
    private Boolean active;

}
