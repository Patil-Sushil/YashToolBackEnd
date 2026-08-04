package com.kalibyte.YashTools.production.machine.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.machine.dto.MachineRequest;
import com.kalibyte.YashTools.production.machine.dto.MachineResponse;
import com.kalibyte.YashTools.production.machine.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION')")
public class MachineController {

    private final MachineService machineService;

    @PostMapping
    public ResponseEntity<ApiResponse<MachineResponse>> create(@Valid @RequestBody MachineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Machine created successfully", machineService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MachineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Machine updated successfully", machineService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(machineService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MachineResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MachineResponse> result = machineService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        machineService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Machine deleted successfully", null));
    }
}
