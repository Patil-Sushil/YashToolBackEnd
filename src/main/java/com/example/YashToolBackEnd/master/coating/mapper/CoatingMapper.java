// java
package com.example.YashToolBackEnd.master.coating.mapper;

import com.example.YashToolBackEnd.master.coating.dto.CoatingRequest;
import com.example.YashToolBackEnd.master.coating.dto.CoatingResponse;
import com.example.YashToolBackEnd.master.coating.entity.Coating;

public class CoatingMapper {
    public static Coating toEntity(CoatingRequest request) {
        return Coating.builder()
                .name(request.getName())
                .rate(request.getRate())
                .active(true)
                .build();
    }

    public static CoatingResponse toResponse(Coating coating) {
        return CoatingResponse.builder()
                .id(coating.getId())
                .name(coating.getName())
                .rate(coating.getRate() == null ? null : String.valueOf(coating.getRate()))
                .active(coating.getActive())
                .build();
    }
}
