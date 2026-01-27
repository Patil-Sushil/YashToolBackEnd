package com.example.YashToolBackEnd.master.rawmaterial.controller;

import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialRequest;
import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialResponse;
import com.example.YashToolBackEnd.master.rawmaterial.service.RawMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masters/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController {

    private final RawMaterialService service;

    @PostMapping
    public RawMaterialResponse create(@Valid @RequestBody RawMaterialRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<RawMaterialResponse> getAll() {
        return service.getAllActive();
    }

    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}
