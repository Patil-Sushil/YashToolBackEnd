package com.kalibyte.YashTools.inventory.transaction.cutpiece.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.dto.CutPieceResponse;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.service.CutPieceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/cut-pieces")
public class CutPieceController {

    private final CutPieceService cutPieceService;

    public CutPieceController(CutPieceService cutPieceService) {
        this.cutPieceService = cutPieceService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<CutPieceResponse>> getCutPieceById(@PathVariable UUID id) {
        CutPieceResponse response = cutPieceService.getCutPieceById(id);
        return ResponseEntity.ok(ApiResponse.success("Cut piece retrieved successfully", response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<CutPieceResponse>> getCutPieceByCode(@PathVariable String code) {
        CutPieceResponse response = cutPieceService.getCutPieceByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Cut piece retrieved successfully", response));
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<CutPieceResponse>>> getRecommendations(
            @RequestParam UUID itemId,
            @RequestParam UUID materialGradeId,
            @RequestParam BigDecimal requiredLength) {
        List<CutPieceResponse> response = cutPieceService.getRecommendations(itemId, materialGradeId, requiredLength);
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<CutPieceResponse>>> listCutPieces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CutPieceResponse> response = cutPieceService.getAllCutPieces(page, size);
        return ResponseEntity.ok(ApiResponse.success("Cut pieces retrieved successfully", response));
    }
}
