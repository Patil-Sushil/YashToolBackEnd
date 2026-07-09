package com.kalibyte.YashTools.quotation.client;

import com.kalibyte.YashTools.common.enums
        .OrderType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionCoolantHoleRodPrice;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionRodNetPrice;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionCoolantHoleRodPriceRepository;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionRodNetPriceRepository;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import com.kalibyte.YashTools.quotation.pricing.PricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateChartClientTest {

    @Mock
    private HyperionRodNetPriceRepository rodRepo;

    @Mock
    private HyperionCoolantHoleRodPriceRepository coolantRepo;

    private RateChartClient rateChartClient;

    @BeforeEach
    void setUp() {
        rateChartClient = new RateChartClient(rodRepo, coolantRepo);
    }

    @Test
    void enrichContext_ExactMatch() {
        // Arrange
        String itemName = "3.2X330MM UG CARBIDE ROD";
        PricingContext ctx = PricingContext.builder()
                .orderType(OrderType.NEW_TOOL)
                .toolName("Solid Carbide Endmill 10mm")
                .itemName(itemName)
                .materialGrade(MaterialGrade.K40UF_H10F)
                .quantity(10)
                .build();

        HyperionRodNetPrice dbRecord = new HyperionRodNetPrice();
        dbRecord.setItem("3.2X330MM UG CARBIDE ROD");
        dbRecord.setK40ufH10f(BigDecimal.valueOf(1380.00));
        dbRecord.setActive(true);

        when(rodRepo.findByItemIgnoreCaseAndActiveTrue(itemName))
                .thenReturn(Optional.of(dbRecord));

        // Act
        PricingContext result = rateChartClient.enrichContext(ctx);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1380.00), result.getRatePerUnit());
        assertEquals("3.2X330MM UG CARBIDE ROD", result.getRateChartItem());
        verify(rodRepo, times(1)).findByItemIgnoreCaseAndActiveTrue(itemName);
        verifyNoMoreInteractions(rodRepo);
    }

    @Test
    void enrichContext_NormalizedMatch() {
        // Arrange
        String itemName = "3.2X330MM  ug carbide  rod";
        PricingContext ctx = PricingContext.builder()
                .orderType(OrderType.NEW_TOOL)
                .toolName("Solid Carbide Endmill 10mm")
                .itemName(itemName)
                .materialGrade(MaterialGrade.K40UF_H10F)
                .quantity(10)
                .build();

        HyperionRodNetPrice dbRecord = new HyperionRodNetPrice();
        dbRecord.setItem("3.2X330MM UG CARBIDE ROD");
        dbRecord.setK40ufH10f(BigDecimal.valueOf(1380.00));
        dbRecord.setActive(true);

        String normalizedName = "3.2X330MM UG CARBIDE ROD";

        // First call with exact itemName returns empty
        when(rodRepo.findByItemIgnoreCaseAndActiveTrue(itemName))
                .thenReturn(Optional.empty());
        // Second call with normalized itemName returns DB record
        when(rodRepo.findByItemIgnoreCaseAndActiveTrue(normalizedName))
                .thenReturn(Optional.of(dbRecord));

        // Act
        PricingContext result = rateChartClient.enrichContext(ctx);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1380.00), result.getRatePerUnit());
        assertEquals("3.2X330MM UG CARBIDE ROD", result.getRateChartItem());
        verify(rodRepo).findByItemIgnoreCaseAndActiveTrue(itemName);
        verify(rodRepo).findByItemIgnoreCaseAndActiveTrue(normalizedName);
    }

    @Test
    void enrichContext_FallbackToStrippedItemKey() {
        // Arrange
        String itemName = "3.2X330MM UG CARBIDE ROD";
        PricingContext ctx = PricingContext.builder()
                .orderType(OrderType.NEW_TOOL)
                .toolName("Solid Carbide Endmill 10mm")
                .itemName(itemName)
                .materialGrade(MaterialGrade.K40UF_H10F)
                .quantity(10)
                .build();

        HyperionRodNetPrice dbRecord = new HyperionRodNetPrice();
        dbRecord.setItem("UG CARBIDE ROD");
        dbRecord.setK40ufH10f(BigDecimal.valueOf(1000.00));
        dbRecord.setActive(true);

        // Exact and normalized lookups return empty
        when(rodRepo.findByItemIgnoreCaseAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());
        
        // Lookup using parsed itemKey ("UG CARBIDE ROD") returns the record
        when(rodRepo.findByItemIgnoreCaseAndActiveTrue("UG CARBIDE ROD"))
                .thenReturn(Optional.of(dbRecord));

        // Act
        PricingContext result = rateChartClient.enrichContext(ctx);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000.00), result.getRatePerUnit());
        assertEquals("UG CARBIDE ROD", result.getRateChartItem());
    }

    @Test
    void enrichContext_NotFound_ThrowsPricingException() {
        // Arrange
        String itemName = "Nonexistent Rod";
        PricingContext ctx = PricingContext.builder()
                .orderType(OrderType.NEW_TOOL)
                .toolName("Solid Carbide Endmill 10mm")
                .itemName(itemName)
                .materialGrade(MaterialGrade.K40UF_H10F)
                .quantity(10)
                .build();

        when(rodRepo.findByItemIgnoreCaseAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        PricingException ex = assertThrows(PricingException.class, () -> {
            rateChartClient.enrichContext(ctx);
        });

        assertTrue(ex.getMessage().contains("Rate not found for item: " + itemName));
    }

    @Test
    void enrichContext_CoolantHoleGradeMatch() {
        // Arrange
        String itemName = "8.2X330MM UG CARBIDE ROD";
        PricingContext ctx = PricingContext.builder()
                .orderType(OrderType.NEW_TOOL)
                .toolName("Solid Carbide Endmill 10mm")
                .itemName(itemName)
                .materialGrade(MaterialGrade.TWO_THREE_HOLE_K40_330)
                .quantity(5)
                .build();

        HyperionCoolantHoleRodPrice coolantRecord = new HyperionCoolantHoleRodPrice();
        coolantRecord.setItem("8.2X330MM UG CARBIDE ROD");
        coolantRecord.setCategory(MaterialGrade.TWO_THREE_HOLE_K40_330.getDescription());
        coolantRecord.setPrice(BigDecimal.valueOf(11267.00));
        coolantRecord.setActive(true);

        when(coolantRepo.findByCategoryIgnoreCaseAndItemIgnoreCase(
                eq(MaterialGrade.TWO_THREE_HOLE_K40_330.getDescription()), eq(itemName)))
                .thenReturn(Optional.of(coolantRecord));

        // Act
        PricingContext result = rateChartClient.enrichContext(ctx);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(11267.00), result.getRatePerUnit());
        assertEquals("8.2X330MM UG CARBIDE ROD", result.getRateChartItem());
        verify(coolantRepo).findByCategoryIgnoreCaseAndItemIgnoreCase(anyString(), eq(itemName));
        verifyNoInteractions(rodRepo);
    }

    @Test
    void materialGrade_JsonCreatorMatchesNameAndDescription() {
        // Test JsonCreator with exact name
        assertEquals(MaterialGrade.K40UF_H10F, MaterialGrade.fromString("K40UF_H10F"));
        assertEquals(MaterialGrade.K40UF_H10F, MaterialGrade.fromString("k40ufh10f"));

        // Test JsonCreator with exact description
        assertEquals(MaterialGrade.TWO_THREE_HOLE_K40_330, MaterialGrade.fromString("2 Hole/3 Hole,30 Degree/40 Degree, Grade K-40,330mm"));
        assertEquals(MaterialGrade.CENTRAL_PARALLEL_K40_330, MaterialGrade.fromString("Central / Parallel hole, Grade K-40, Length 330mm"));
        assertEquals(MaterialGrade.K40UF_H10F, MaterialGrade.fromString("k40ufH10f"));

        // Test invalid grade
        assertThrows(IllegalArgumentException.class, () -> MaterialGrade.fromString("Invalid Grade"));
    }
}
