package com.kalibyte.YashTools.enquiry.service.impl;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.customer.repository.CustomerRepository;
import com.kalibyte.YashTools.enquiry.dto.request.CreateEnquiryRequest;
import com.kalibyte.YashTools.enquiry.dto.request.UpdateEnquiryStatusRequest;
import com.kalibyte.YashTools.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.YashTools.enquiry.mapper.EnquiryMapper;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import com.kalibyte.YashTools.enquiry.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryValidationService validationService;
    private final EnquiryMapper enquiryMapper;

    @Override
    @Transactional
    @LoggableAction("CREATE_ENQUIRY")
    public EnquiryResponse createEnquiry(CreateEnquiryRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + request.getCustomerId()
                        )
                );

        Enquiry enquiry = enquiryMapper.toEntity(request, customer);
        
        // Automatically associate the Enquiry with the Customer's company
        if (customer.getCompany() != null) {
            enquiry.setCompany(customer.getCompany());
        }
        
        enquiry.setEnquiryNo(generateEnquiryNumber(customer.getCompany() != null ? customer.getCompany().getCode() : "YT"));
        enquiry.setStatus(EnquiryStatus.CREATED);

        validationService.validate(enquiry);

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);
        return enquiryMapper.toResponse(savedEnquiry);
    }

    @Override
    public EnquiryResponse getById(UUID enquiryId) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Enquiry not found with id: " + enquiryId
                        )
                );
        return enquiryMapper.toResponse(enquiry);
    }

    @Override
    public List<EnquiryResponse> getAllEnquiries() {
        return enquiryRepository.findAll()
                .stream()
                .map(enquiryMapper::toResponse)
                .toList();
    }

    @Override
    public List<EnquiryResponse> getEnquiriesByCustomer(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        }
        return enquiryRepository.findByCustomerId(customerId)
                .stream()
                .map(enquiryMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @LoggableAction("UPDATE_ENQUIRY_STATUS")
    public EnquiryResponse updateEnquiryStatus(UUID enquiryId, UpdateEnquiryStatusRequest request) {
        log.info("Updating status for enquiry ID: {} to {}", enquiryId, request.getStatus());

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enquiry not found with ID: " + enquiryId
                ));

        // Validate status transition business rules
        validateStatusTransition(enquiry.getStatus(), request.getStatus());

        enquiry.setStatus(request.getStatus());

        // Append status change remarks if provided
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            String existingRemarks = enquiry.getItems().isEmpty() ? null : enquiry.getItems().get(0).getRemarks();
            String updatedRemarks = existingRemarks != null
                    ? existingRemarks + " | STATUS CHANGE [" + request.getStatus() + "]: " + request.getRemarks()
                    : "STATUS CHANGE [" + request.getStatus() + "]: " + request.getRemarks();

            if (!enquiry.getItems().isEmpty()) {
                enquiry.getItems().get(0).setRemarks(updatedRemarks);
            }
        }

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);

        log.info("Enquiry {} status updated successfully to {}", savedEnquiry.getEnquiryNo(), savedEnquiry.getStatus());

        return enquiryMapper.toResponse(savedEnquiry);
    }

    /**
     * Validate business rules for status transitions
     */
    private void validateStatusTransition(EnquiryStatus currentStatus, EnquiryStatus newStatus) {
        if (currentStatus == EnquiryStatus.CLOSED) {
            throw new BusinessException(
                    "Cannot update status of a closed enquiry. Please create a new enquiry."
            );
        }

        if (currentStatus == newStatus) {
            throw new BusinessException(
                    "New status must be different from current status."
            );
        }

        // Define allowed transitions
        boolean isValidTransition = switch (currentStatus) {
            case CREATED -> newStatus == EnquiryStatus.UNDER_REVIEW ||
                    newStatus == EnquiryStatus.QUOTED ||
                    newStatus == EnquiryStatus.CLOSED;

            case UNDER_REVIEW -> newStatus == EnquiryStatus.QUOTED ||
                    newStatus == EnquiryStatus.CLOSED;

            case QUOTED -> newStatus == EnquiryStatus.ACCEPTED ||
                    newStatus == EnquiryStatus.CLOSED;

            case ACCEPTED -> newStatus == EnquiryStatus.CLOSED;

            default -> false;
        };

        if (!isValidTransition) {
            throw new BusinessException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private String generateEnquiryNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<Enquiry> lastEnquiry = enquiryRepository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastEnquiry.isPresent()) {
            String lastNumber = lastEnquiry.get().getEnquiryNo();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 3) {
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1 if parsing fails
                }
            }
        }

        String companyPrefix = companyCode;
        if (companyPrefix == null || companyPrefix.trim().isEmpty()) {
            companyPrefix = "YT";
        }

        return String.format("%s-ENQ-%d-%04d", companyPrefix, year, nextNumber);
    }
}