package com.kalibyte.YashTools.inventory.transaction.materialissue.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueStatus;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueType;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.repository.CutPieceRepository;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.service.CutPieceService;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueRequest;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.entity.MaterialIssue;
import com.kalibyte.YashTools.inventory.transaction.materialissue.mapper.MaterialIssueMapper;
import com.kalibyte.YashTools.inventory.transaction.materialissue.repository.MaterialIssueRepository;
import com.kalibyte.YashTools.inventory.transaction.materialissue.service.MaterialIssueService;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;

import java.math.BigDecimal;
import java.util.UUID;

import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;

@Service
public class MaterialIssueServiceImpl implements MaterialIssueService {

    private final MaterialIssueRepository repository;
    private final MaterialIssueMapper mapper;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    private final CutPieceRepository cutPieceRepository;
    private final JobCardRepository jobCardRepository;
    private final WorkOrderRepository workOrderRepository;
    
    private final StockService stockService;
    private final CutPieceService cutPieceService;
    private final StockTransactionService stockTransactionService;

    public MaterialIssueServiceImpl(MaterialIssueRepository repository, MaterialIssueMapper mapper,
                                   ItemRepository itemRepository, MaterialGradeRepository materialGradeRepository,
                                   CutPieceRepository cutPieceRepository, JobCardRepository jobCardRepository,
                                   WorkOrderRepository workOrderRepository,
                                   StockService stockService, CutPieceService cutPieceService,
                                   StockTransactionService stockTransactionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.materialGradeRepository = materialGradeRepository;
        this.cutPieceRepository = cutPieceRepository;
        this.jobCardRepository = jobCardRepository;
        this.workOrderRepository = workOrderRepository;
        this.stockService = stockService;
        this.cutPieceService = cutPieceService;
        this.stockTransactionService = stockTransactionService;
    }

    @Override
    @Transactional
    public MaterialIssueResponse issueMaterial(MaterialIssueRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));
        
        MaterialGrade materialGrade = materialGradeRepository.findById(request.getMaterialGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + request.getMaterialGradeId()));

        JobCard jobCard = null;
        if (request.getJobCardId() != null) {
            jobCard = jobCardRepository.findById(request.getJobCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job Card not found with ID: " + request.getJobCardId()));
        }

        long count = repository.count();
        String issueNumber = String.format("ISS-%06d", count + 1);
        
        CutPiece cutPiece = null;
        CutPiece newCutPiece = null;
        BigDecimal issuedLength = request.getRequiredLength();

        if (request.getIssueType() == MaterialIssueType.FULL_ROD) {
            // Deduct 1 Full Rod from stock
            stockService.deductStock(item, materialGrade, BigDecimal.ONE);

            if (request.getFullRodLength() == null || request.getFullRodLength().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Full Rod length is required and must be greater than zero when issuing a full rod");
            }

            if (request.getRequiredLength().compareTo(request.getFullRodLength()) > 0) {
                throw new BusinessException("Required length cannot exceed full rod length");
            }

            // If partially consumed, create a new CutPiece
            if (request.getRequiredLength().compareTo(request.getFullRodLength()) < 0) {
                BigDecimal remainingLength = request.getFullRodLength().subtract(request.getRequiredLength());
                newCutPiece = cutPieceService.createCutPiece(item, materialGrade, remainingLength);
            }
            
            // Record Stock Transaction
            stockTransactionService.createTransaction(
                    StockTransactionType.MATERIAL_ISSUE,
                    item,
                    materialGrade,
                    BigDecimal.ONE,
                    issueNumber,
                    "Issued 1 Full Rod of length: " + request.getFullRodLength() + ", Consumed: " + request.getRequiredLength()
            );
        } else {
            // Issuing an existing Cut Piece
            if (request.getCutPieceId() == null) {
                throw new BusinessException("Cut Piece ID is required when issue type is CUT_PIECE");
            }

            cutPiece = cutPieceRepository.findById(request.getCutPieceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cut Piece not found with ID: " + request.getCutPieceId()));

            if (!cutPiece.getItem().getId().equals(item.getId()) ||
                !cutPiece.getMaterialGrade().getId().equals(materialGrade.getId())) {
                throw new BusinessException("Cut piece details do not match item or grade selection");
            }

            // Deduct length from the Cut Piece
            cutPieceService.updateCutPieceLength(request.getCutPieceId(), request.getRequiredLength());

            // Record Stock Transaction (Delta quantity in full rods is 0, but audit history is preserved)
            stockTransactionService.createTransaction(
                    StockTransactionType.MATERIAL_ISSUE,
                    item,
                    materialGrade,
                    BigDecimal.ZERO,
                    issueNumber,
                    "Issued length " + request.getRequiredLength() + " from Cut Piece: " + cutPiece.getCode()
            );
        }

        MaterialIssue issue = MaterialIssue.builder()
                .issueNumber(issueNumber)
                .item(item)
                .materialGrade(materialGrade)
                .issueType(request.getIssueType())
                .cutPiece(cutPiece)
                .requiredLength(request.getRequiredLength())
                .issuedLength(issuedLength)
                .newCutPiece(newCutPiece)
                .jobCard(jobCard)
                .status(MaterialIssueStatus.ISSUED)
                .build();

        if (jobCard != null && (jobCard.getStatus() == JobCardStatus.CREATED || jobCard.getStatus() == JobCardStatus.PLANNED)) {
            jobCard.setStatus(JobCardStatus.ASSIGNED);
            jobCardRepository.save(jobCard);
        }

        if (jobCard != null && jobCard.getWorkOrder() != null && jobCard.getWorkOrder().getStatus() == WorkOrderStatus.CREATED) {
            jobCard.getWorkOrder().setStatus(WorkOrderStatus.IN_PROGRESS);
            workOrderRepository.save(jobCard.getWorkOrder());
        }

        return mapper.toResponse(repository.save(issue));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialIssueResponse getIssueById(UUID id) {
        MaterialIssue issue = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material issue not found with ID: " + id));
        return mapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialIssueResponse getIssueByNumber(String issueNumber) {
        MaterialIssue issue = repository.findByIssueNumber(issueNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Material issue not found with number: " + issueNumber));
        return mapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MaterialIssueResponse> getAllIssues(int page, int size) {
        Page<MaterialIssue> issuePage = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(issuePage, mapper::toResponse);
    }
}
