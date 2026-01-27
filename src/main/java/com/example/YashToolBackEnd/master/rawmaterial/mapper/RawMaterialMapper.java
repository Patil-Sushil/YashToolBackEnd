package com.example.YashToolBackEnd.master.rawmaterial.mapper;


import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialRequest;
import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialResponse;
import com.example.YashToolBackEnd.master.rawmaterial.entity.RawMaterial;

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
