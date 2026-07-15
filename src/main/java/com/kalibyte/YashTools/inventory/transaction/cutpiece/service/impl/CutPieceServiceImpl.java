package com.kalibyte.YashTools.inventory.transaction.cutpiece.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.CutPieceStatus;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.dto.CutPieceResponse;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.mapper.CutPieceMapper;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.repository.CutPieceRepository;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.service.CutPieceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CutPieceServiceImpl implements CutPieceService {

    private final CutPieceRepository repository;
    private final CutPieceMapper mapper;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;

    public CutPieceServiceImpl(CutPieceRepository repository, CutPieceMapper mapper,
                              ItemRepository itemRepository, MaterialGradeRepository materialGradeRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.materialGradeRepository = materialGradeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CutPieceResponse getCutPieceById(UUID id) {
        CutPiece cp = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cut piece not found with ID: " + id));
        return mapper.toResponse(cp);
    }

    @Override
    @Transactional(readOnly = true)
    public CutPieceResponse getCutPieceByCode(String code) {
        CutPiece cp = repository.findByCode(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Cut piece not found with Code: " + code));
        return mapper.toResponse(cp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CutPieceResponse> getRecommendations(UUID itemId, UUID materialGradeId, BigDecimal requiredLength) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));
        MaterialGrade materialGrade = materialGradeRepository.findById(materialGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + materialGradeId));

        if (requiredLength == null || requiredLength.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Required length must be greater than zero");
        }

        List<CutPiece> recommended = repository
                .findByItemAndMaterialGradeAndStatusAndRemainingLengthGreaterThanEqualOrderByRemainingLengthAsc(
                        item, materialGrade, CutPieceStatus.AVAILABLE, requiredLength);

        return recommended.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CutPieceResponse> getAllCutPieces(int page, int size) {
        Page<CutPiece> cutPiecePage = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(cutPiecePage, mapper::toResponse);
    }

    @Override
    @Transactional
    public CutPiece createCutPiece(Item item, MaterialGrade grade, BigDecimal remainingLength) {
        if (remainingLength == null || remainingLength.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Remaining length must be greater than zero");
        }

        long count = repository.count();
        String generatedCode = String.format("CP-%06d", count + 1);

        CutPiece cp = CutPiece.builder()
                .code(generatedCode)
                .item(item)
                .materialGrade(grade)
                .remainingLength(remainingLength)
                .status(CutPieceStatus.AVAILABLE)
                .build();

        return repository.save(cp);
    }

    @Override
    @Transactional
    public void updateCutPieceLength(UUID cutPieceId, BigDecimal lengthConsumed) {
        CutPiece cp = repository.findById(cutPieceId)
                .orElseThrow(() -> new ResourceNotFoundException("Cut piece not found with ID: " + cutPieceId));

        if (cp.getStatus() != CutPieceStatus.AVAILABLE) {
            throw new BusinessException("Cut piece is not available for consumption");
        }

        if (lengthConsumed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Length consumed must be greater than zero");
        }

        if (cp.getRemainingLength().compareTo(lengthConsumed) < 0) {
            throw new BusinessException("Insufficient length on cut piece: " + cp.getCode() + 
                                       ". Available: " + cp.getRemainingLength() + ", Required: " + lengthConsumed);
        }

        BigDecimal newLength = cp.getRemainingLength().subtract(lengthConsumed);
        cp.setRemainingLength(newLength);

        if (newLength.compareTo(BigDecimal.ZERO) == 0) {
            cp.setStatus(CutPieceStatus.CONSUMED);
        }

        repository.save(cp);
    }
}
