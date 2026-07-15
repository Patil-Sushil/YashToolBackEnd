package com.kalibyte.YashTools.inventory.transaction.cutpiece.repository;

import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.CutPieceStatus;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CutPieceRepository extends JpaRepository<CutPiece, UUID> {

    Optional<CutPiece> findByCode(String code);

    List<CutPiece> findByItemAndMaterialGradeAndStatusAndRemainingLengthGreaterThanEqualOrderByRemainingLengthAsc(
            Item item,
            MaterialGrade materialGrade,
            CutPieceStatus status,
            BigDecimal minLength
    );
}
