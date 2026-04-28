package com.kalibyte.YashTools.master.rawmaterial.mapper;


import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;
import com.kalibyte.YashTools.master.rawmaterial.entity.RawMaterial;

public class RawMaterialMapper {

    public static RawMaterial toEntity(RawMaterialRequest request){
        return RawMaterial.builder()
                .name(request.getName())
                .rate(request.getRate())
                .active(true)
                .build();
    }
    public static RawMaterialResponse toResponse(RawMaterial material) {
        return RawMaterialResponse.builder()
                .id(material.getId())
                .name(material.getName())
                .rate(material.getRate())
                .active(material.getActive())
                .build();
    }
}
