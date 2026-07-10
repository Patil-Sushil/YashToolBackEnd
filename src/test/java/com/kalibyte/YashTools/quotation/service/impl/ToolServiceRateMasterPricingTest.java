package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.enums.CoatingType;
import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialType;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import com.kalibyte.YashTools.master.ratechart.service.ToolServiceRateMasterService;
import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.dto.request.QuotationItemSpecsRequest;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;
import com.kalibyte.YashTools.quotation.pricing.PricingStrategyFactory;
import com.kalibyte.YashTools.quotation.client.RateChartClient;
import com.kalibyte.YashTools.quotation.validator.QuotationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolServiceRateMasterPricingTest {

    @Mock
    private RateChartClient rateChartClient;
    @Mock
    private PricingStrategyFactory strategyFactory;
    @Mock
    private QuotationValidator validator;
    @Mock
    private ToolServiceRateMasterService toolServiceRateMasterService;

    @InjectMocks
    private PricingEngineServiceImpl pricingEngineService;

    private ToolServiceRateMaster reformingRate;

    @BeforeEach
    void setUp() {
        reformingRate = ToolServiceRateMaster.builder()
                .serviceCode("SRV-00001")
                .serviceType(ServiceType.RE_FORMING)
                .toolType("End Mill")
                .toolMaterial("Carbide")
                .diameterFrom(3.0)
                .diameterTo(12.0)
                .baseRate(BigDecimal.valueOf(450.0))
                .minorDamageCharge(BigDecimal.valueOf(150.0))
                .mediumDamageCharge(BigDecimal.valueOf(250.0))
                .majorDamageCharge(BigDecimal.valueOf(400.0))
                .coatingTiN(BigDecimal.valueOf(100.0))
                .coatingTiAlN(BigDecimal.valueOf(150.0))
                .coatingAlCrN(BigDecimal.valueOf(220.0))
                .coatingDlc(BigDecimal.valueOf(250.0))
                .specialGeometryCharge(BigDecimal.valueOf(200.0))
                .specialProfileCharge(BigDecimal.valueOf(200.0))
                .expressDeliveryCharge(BigDecimal.valueOf(150.0))
                .standardDeliveryDays(5)
                .active(true)
                .build();
        reformingRate.setId(UUID.randomUUID());
    }

    @Test
    void testPriceReformingItem_Success() {
        // Arrange
        QuotationItemSpecsRequest specs = QuotationItemSpecsRequest.builder()
                .diameter(8.0)
                .materialType(MaterialType.CARBIDE)
                .coatingRequired(true)
                .coatingType(CoatingType.VICIOUS_BROWN) // Maps to AlCrN
                .damageLevel("Medium")
                .specialProfile(false)
                .expressDelivery(true)
                .build();

        QuotationItemRequest req = QuotationItemRequest.builder()
                .orderType(OrderType.REFORMING)
                .toolName("End Mill")
                .itemName("Carbide End Mill 8mm")
                .quantity(2)
                .overallLength(80.0)
                .userMultiplier(BigDecimal.ONE)
                .specs(specs)
                .build();

        when(toolServiceRateMasterService.findMatchingRate(ServiceType.RE_FORMING, "End Mill", "Carbide", 8.0))
                .thenReturn(reformingRate);

        // Act
        PricingBreakdown breakdown = pricingEngineService.priceItem(req);

        // Assert
        assertNotNull(breakdown);
        assertEquals(0, BigDecimal.valueOf(450.0).compareTo(breakdown.getRatePerUnit())); // Base rate
        assertEquals(0, BigDecimal.valueOf(220.0).compareTo(breakdown.getCoatingCharge())); // AlCrN charge
        // Base(450) + Medium(250) + Express(150) + AlCrN(220) = 1070
        assertEquals(0, BigDecimal.valueOf(1070.0).compareTo(breakdown.getUnitPrice()));
        assertEquals(0, BigDecimal.valueOf(2140.0).compareTo(breakdown.getLineSubtotal()));
        assertEquals("tool_service_rate_masters", breakdown.getRateSourceTable());
        assertEquals("SRV-00001", breakdown.getRateChartItem());

        verify(validator).validateItem(req, 1);
        verify(toolServiceRateMasterService).findMatchingRate(ServiceType.RE_FORMING, "End Mill", "Carbide", 8.0);
    }
}
