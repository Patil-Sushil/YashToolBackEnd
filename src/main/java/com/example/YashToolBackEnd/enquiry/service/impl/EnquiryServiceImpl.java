package com.example.YashToolBackEnd.enquiry.service.impl;

import com.example.YashToolBackEnd.common.enums.EnquiryStatus;
import com.example.YashToolBackEnd.common.exception.BusinessException;
import com.example.YashToolBackEnd.common.exception.BusinessValidationException;
import com.example.YashToolBackEnd.common.exception.ResourceNotFoundException;
import com.example.YashToolBackEnd.common.util.NumberGeneratorUtil;
import com.example.YashToolBackEnd.customer.entity.Customer;
import com.example.YashToolBackEnd.customer.repository.CustomerRepository;
import com.example.YashToolBackEnd.enquiry.dto.request.CreateEnquiryRequest;
import com.example.YashToolBackEnd.enquiry.dto.request.EnquiryItemRequest;
import com.example.YashToolBackEnd.enquiry.entity.*;
import com.example.YashToolBackEnd.enquiry.dto.response.EnquiryResponse;
import com.example.YashToolBackEnd.enquiry.mapper.EnquiryMapper;
import com.example.YashToolBackEnd.enquiry.repository.EnquiryRepository;
import com.example.YashToolBackEnd.enquiry.service.EnquiryService;
import com.example.YashToolBackEnd.enquiry.service.EnquiryValidationService;
import com.example.YashToolBackEnd.master.coating.entity.Coating;
import com.example.YashToolBackEnd.master.coating.repository.CoatingRepository;
import com.example.YashToolBackEnd.master.rawmaterial.entity.RawMaterial;
import com.example.YashToolBackEnd.master.rawmaterial.repository.RawMaterialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryValidationService validationService;
    private final RawMaterialRepository rawMaterialRepository;
    private final CoatingRepository coatingRepository;

    @Override
    @Transactional
    public EnquiryResponse createEnquiry(CreateEnquiryRequest request) {


           // Fetch Customer (read-only)

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));


           // Map DTO → Entity (NO master injection here)

        Enquiry enquiry = EnquiryMapper.toEntity(request, customer);
        enquiry.setEnquiryNo(NumberGeneratorUtil.generate("ENQ"));
        enquiry.setStatus(EnquiryStatus.CREATED);


           // Inject Master Entities (CRITICAL STEP)

        injectMasterData(enquiry, request);


           // Validate Business Rules (AFTER injection)

        validationService.validate(enquiry);


           // Save atomically

        Enquiry saved = enquiryRepository.save(enquiry);
        return EnquiryMapper.toResponse(saved);
    }

    @Override
    public EnquiryResponse getById(Long enquiryId) {

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Enquiry not found with id: " + enquiryId));

        return EnquiryMapper.toResponse(enquiry);
    }

    @Override
    public List<EnquiryResponse> getAllEnquiries() {

        return enquiryRepository.findAll()
                .stream()
                .map(EnquiryMapper::toResponse)
                .toList();
    }


    // MASTER ENTITY INJECTION LOGIC

    private void injectMasterData(Enquiry enquiry, CreateEnquiryRequest request) {

        for (int i = 0; i < enquiry.getItems().size(); i++) {

            EnquiryItem item = enquiry.getItems().get(i);
            EnquiryItemRequest itemReq = request.getItems().get(i);

            switch (item.getOrderType()) {

                case NEW_TOOL -> {
                    NewToolSpecs specs = item.getNewToolSpecs();
                    if (specs == null) {
                        throw new BusinessException(
                                "New tool specs missing");
                    }

                    //  Raw Material (MANDATORY)
                    Long rawMaterialId =
                            itemReq.getNewToolSpecs().getRawMaterialId();

                    RawMaterial rawMaterial =
                            rawMaterialRepository.findById(rawMaterialId)
                                    .orElseThrow(() ->
                                            new BusinessException(
                                                    "Invalid raw material"));

                    specs.setRawMaterial(rawMaterial);

                    //  Coating (CONDITIONAL)
                    if (Boolean.TRUE.equals(specs.getHasCoating())) {

                        Long coatingId =
                                itemReq.getNewToolSpecs().getCoatingId();

                        if (coatingId == null) {
                            throw new BusinessValidationException(
                                    "Coating is mandatory when hasCoating=true");
                        }

                        Coating coating =
                                coatingRepository.findById(coatingId)
                                        .orElseThrow(() ->
                                                new BusinessValidationException(
                                                        "Invalid coating"));

                        specs.setCoating(coating);
                    }
                }

                case RESHARPENING -> {
                    ResharpeningSpecs specs = item.getResharpeningSpecs();

                    if (specs != null &&
                            Boolean.TRUE.equals(specs.getHasCoating())) {

                        Long coatingId =
                                itemReq.getResharpeningSpecs().getCoatingId();

                        Coating coating =
                                coatingRepository.findById(coatingId)
                                        .orElseThrow(() ->
                                                new BusinessException(
                                                        "Invalid coating"));

                        specs.setCoating(coating);
                    }
                }

                case REFORMING -> {
                    ReformingSpecs specs = item.getReformingSpecs();

                    if (specs != null &&
                            Boolean.TRUE.equals(specs.getHasCoating())) {

                        Long coatingId =
                                itemReq.getReformingSpecs().getCoatingId();

                        Coating coating =
                                coatingRepository.findById(coatingId)
                                        .orElseThrow(() ->
                                                new BusinessException(
                                                        "Invalid coating"));

                        specs.setCoating(coating);
                    }
                }
            }
        }
    }
}
