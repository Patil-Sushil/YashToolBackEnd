package com.kalibyte.YashTools.inventory.transaction.cutpiece.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.dto.CutPieceResponse;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CutPieceService {
    CutPieceResponse getCutPieceById(UUID id);
    CutPieceResponse getCutPieceByCode(String code);
    List<CutPieceResponse> getRecommendations(UUID itemId, UUID materialGradeId, BigDecimal requiredLength);
    PageResponse<CutPieceResponse> getAllCutPieces(int page, int size);

    // Internal methods used by Material Issue Service
    CutPiece createCutPiece(Item item, MaterialGrade grade, BigDecimal remainingLength);
    void updateCutPieceLength(UUID cutPieceId, BigDecimal lengthConsumed);
}
