package com.example.YashToolBackEnd.master.coating.controller;

import com.example.YashToolBackEnd.master.coating.dto.CoatingRequest;
import com.example.YashToolBackEnd.master.coating.dto.CoatingResponse;
import com.example.YashToolBackEnd.master.coating.service.CoatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masters/coatings")
@RequiredArgsConstructor
public class CoatingController {
    private final CoatingService service;

    @PostMapping
    public CoatingResponse create(@Valid @RequestBody CoatingRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<CoatingResponse> getAll() {
        return service.getAllActive();

    }

    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}
