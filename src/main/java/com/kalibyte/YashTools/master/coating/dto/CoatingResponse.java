package com.kalibyte.YashTools.master.coating.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoatingResponse {
    private Long id;
    private String name;
    private String rate;
    private Boolean active;

}
