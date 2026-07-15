package com.kalibyte.YashTools.inventory;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueType;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.repository.CutPieceRepository;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.service.CutPieceService;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueRequest;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.entity.MaterialIssue;
import com.kalibyte.YashTools.inventory.transaction.materialissue.mapper.MaterialIssueMapper;
import com.kalibyte.YashTools.inventory.transaction.materialissue.repository.MaterialIssueRepository;
import com.kalibyte.YashTools.inventory.transaction.materialissue.service.impl.MaterialIssueServiceImpl;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaterialIssueServiceImplTest {

    @Mock
    private MaterialIssueRepository repository;
    @Mock
    private MaterialIssueMapper mapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private MaterialGradeRepository materialGradeRepository;
    @Mock
    private CutPieceRepository cutPieceRepository;
    @Mock
    private StockService stockService;
    @Mock
    private CutPieceService cutPieceService;
    @Mock
    private StockTransactionService stockTransactionService;

    @InjectMocks
    private MaterialIssueServiceImpl materialIssueService;

    private Item item;
    private MaterialGrade grade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = Item.builder().name("Insert").sku("INS-1").build();
        item.setId(UUID.randomUUID());
        grade = MaterialGrade.builder().name("K40UF").code("K40").build();
        grade.setId(UUID.randomUUID());
    }

    @Test
    void issueMaterial_FullRod_PartiallyConsumed() {
        MaterialIssueRequest request = MaterialIssueRequest.builder()
                .itemId(item.getId())
                .materialGradeId(grade.getId())
                .issueType(MaterialIssueType.FULL_ROD)
                .requiredLength(new BigDecimal("600.0"))
                .fullRodLength(new BigDecimal("1000.0"))
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(materialGradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));

        CutPiece createdCutPiece = CutPiece.builder().code("CP-001").remainingLength(new BigDecimal("400.0")).build();
        when(cutPieceService.createCutPiece(eq(item), eq(grade), eq(new BigDecimal("400.0"))))
                .thenReturn(createdCutPiece);

        MaterialIssue issue = MaterialIssue.builder().build();
        when(repository.save(any(MaterialIssue.class))).thenReturn(issue);
        when(mapper.toResponse(any(MaterialIssue.class))).thenReturn(MaterialIssueResponse.builder().issueNumber("ISS-1").build());

        MaterialIssueResponse response = materialIssueService.issueMaterial(request);

        assertNotNull(response);
        verify(stockService, times(1)).deductStock(item, grade, BigDecimal.ONE);
        verify(cutPieceService, times(1)).createCutPiece(item, grade, new BigDecimal("400.0"));
    }

    @Test
    void issueMaterial_FullRod_NoFullRodLength_ThrowsException() {
        MaterialIssueRequest request = MaterialIssueRequest.builder()
                .itemId(item.getId())
                .materialGradeId(grade.getId())
                .issueType(MaterialIssueType.FULL_ROD)
                .requiredLength(new BigDecimal("600.0"))
                .fullRodLength(null) // missing
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(materialGradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));

        assertThrows(BusinessException.class, () -> materialIssueService.issueMaterial(request));
    }
}
