package com.kalibyte.YashTools.report.gst.service;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoiceItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.report.gst.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GstReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final CompanyRepository companyRepository;

    public Gstr1ReportDTO getGstr1Report(LocalDate startDate, LocalDate endDate, String label) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("No active company context found.");
        }

        List<SalesInvoice> invoices = salesInvoiceRepository.findByCompanyIdAndInvoiceDateBetween(companyId, startDate, endDate);

        List<Gstr1ItemDTO> items = invoices.stream()
                .map(inv -> {
                    String customerName = "N/A";
                    String customerGstin = "N/A";
                    if (inv.getWorkOrder() != null && inv.getWorkOrder().getCustomer() != null) {
                        customerName = inv.getWorkOrder().getCustomer().getCompanyName();
                        customerGstin = inv.getWorkOrder().getCustomer().getGstNumber();
                    }

                    return Gstr1ItemDTO.builder()
                            .invoiceNo(inv.getInvoiceNo())
                            .invoiceDate(inv.getInvoiceDate())
                            .customerName(customerName)
                            .customerGstin(customerGstin != null ? customerGstin : "N/A")
                            .taxableValue(inv.getSubTotal())
                            .cgstRate(inv.getCgstRate())
                            .cgstAmount(inv.getCgstAmount())
                            .sgstRate(inv.getSgstRate())
                            .sgstAmount(inv.getSgstAmount())
                            .igstRate(inv.getIgstRate())
                            .igstAmount(inv.getIgstAmount())
                            .totalInvoiceValue(inv.getTotalAmount())
                            .status(inv.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalTaxable = items.stream().map(Gstr1ItemDTO::getTaxableValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCgst = items.stream().map(Gstr1ItemDTO::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSgst = items.stream().map(Gstr1ItemDTO::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIgst = items.stream().map(Gstr1ItemDTO::getIgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGst = totalCgst.add(totalSgst).add(totalIgst);
        BigDecimal totalInvoiceVal = items.stream().map(Gstr1ItemDTO::getTotalInvoiceValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return Gstr1ReportDTO.builder()
                .periodLabel(label)
                .totalTaxableValue(totalTaxable)
                .totalCgstAmount(totalCgst)
                .totalSgstAmount(totalSgst)
                .totalIgstAmount(totalIgst)
                .totalGstAmount(totalGst)
                .totalInvoiceValue(totalInvoiceVal)
                .items(items)
                .build();
    }

    public Gstr2ReportDTO getGstr2Report(LocalDate startDate, LocalDate endDate, String label) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("No active company context found.");
        }

        Company company = companyRepository.findById(companyId).orElse(null);
        String companyGst = company != null ? company.getGstNumber() : "";

        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByCompanyIdAndInvoiceDateBetween(companyId, startDate, endDate);

        List<Gstr2ItemDTO> items = invoices.stream()
                .map(pi -> {
                    BigDecimal taxableValue = pi.getItems().stream()
                            .map(pItem -> pItem.getTaxableAmount() != null ? pItem.getTaxableAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalGst = pi.getItems().stream()
                            .map(pItem -> pItem.getGst() != null ? pItem.getGst() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String vendorName = pi.getVendor() != null ? pi.getVendor().getVendorName() : "N/A";
                    String vendorGst = pi.getVendor() != null ? pi.getVendor().getGstin() : "N/A";

                    Gstr2ItemDTO item = Gstr2ItemDTO.builder()
                            .invoiceNumber(pi.getInvoiceNumber())
                            .supplierInvoiceNumber(pi.getSupplierInvoiceNumber())
                            .invoiceDate(pi.getInvoiceDate())
                            .vendorName(vendorName)
                            .vendorGstin(vendorGst != null ? vendorGst : "N/A")
                            .taxableValue(taxableValue)
                            .totalGstAmount(totalGst)
                            .totalInvoiceValue(pi.getTotalAmount())
                            .status(pi.getStatus() != null ? pi.getStatus().name() : "N/A")
                            .build();

                    populatePurchaseTaxSplit(vendorGst, companyGst, totalGst, item);
                    return item;
                })
                .collect(Collectors.toList());

        BigDecimal totalTaxable = items.stream().map(Gstr2ItemDTO::getTaxableValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCgst = items.stream().map(Gstr2ItemDTO::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSgst = items.stream().map(Gstr2ItemDTO::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIgst = items.stream().map(Gstr2ItemDTO::getIgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGst = items.stream().map(Gstr2ItemDTO::getTotalGstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInvoiceVal = items.stream().map(Gstr2ItemDTO::getTotalInvoiceValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return Gstr2ReportDTO.builder()
                .periodLabel(label)
                .totalTaxableValue(totalTaxable)
                .totalCgstAmount(totalCgst)
                .totalSgstAmount(totalSgst)
                .totalIgstAmount(totalIgst)
                .totalGstAmount(totalGst)
                .totalInvoiceValue(totalInvoiceVal)
                .items(items)
                .build();
    }

    public Gstr3bReportDTO getGstr3bReport(LocalDate startDate, LocalDate endDate, String label) {
        Gstr1ReportDTO sales = getGstr1Report(startDate, endDate, label);
        Gstr2ReportDTO purchase = getGstr2Report(startDate, endDate, label);

        BigDecimal netCgst = sales.getTotalCgstAmount().subtract(purchase.getTotalCgstAmount());
        BigDecimal netSgst = sales.getTotalSgstAmount().subtract(purchase.getTotalSgstAmount());
        BigDecimal netIgst = sales.getTotalIgstAmount().subtract(purchase.getTotalIgstAmount());

        // Under GST, net payable is tax liability minus ITC. 
        // If it's negative, it means carry-forward ITC, which we represent as 0 net tax payable (it's not paid, but carried forward).
        BigDecimal netCgstPayable = netCgst.max(BigDecimal.ZERO);
        BigDecimal netSgstPayable = netSgst.max(BigDecimal.ZERO);
        BigDecimal netIgstPayable = netIgst.max(BigDecimal.ZERO);
        BigDecimal totalNet = netCgstPayable.add(netSgstPayable).add(netIgstPayable);

        return Gstr3bReportDTO.builder()
                .periodLabel(label)
                .outwardTaxableValue(sales.getTotalTaxableValue())
                .outwardCgst(sales.getTotalCgstAmount())
                .outwardSgst(sales.getTotalSgstAmount())
                .outwardIgst(sales.getTotalIgstAmount())
                .totalOutwardTax(sales.getTotalGstAmount())
                .inwardTaxableValue(purchase.getTotalTaxableValue())
                .inwardCgst(purchase.getTotalCgstAmount())
                .inwardSgst(purchase.getTotalSgstAmount())
                .inwardIgst(purchase.getTotalIgstAmount())
                .totalInwardItc(purchase.getTotalGstAmount())
                .netCgstPayable(netCgstPayable)
                .netSgstPayable(netSgstPayable)
                .netIgstPayable(netIgstPayable)
                .totalNetPayable(totalNet)
                .build();
    }

    private void populatePurchaseTaxSplit(String vendorGst, String companyGst, BigDecimal totalGst, Gstr2ItemDTO item) {
        boolean isIntrastate = true;
        if (companyGst != null && companyGst.length() >= 2 && vendorGst != null && vendorGst.length() >= 2) {
            String companyState = companyGst.substring(0, 2);
            String vendorState = vendorGst.substring(0, 2);
            if (!companyState.equals(vendorState)) {
                isIntrastate = false;
            }
        }

        if (isIntrastate) {
            BigDecimal half = totalGst.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
            item.setCgstAmount(half);
            item.setSgstAmount(half);
            item.setIgstAmount(BigDecimal.ZERO);
        } else {
            item.setCgstAmount(BigDecimal.ZERO);
            item.setSgstAmount(BigDecimal.ZERO);
            item.setIgstAmount(totalGst);
        }
    }

    public byte[] exportGstr1ToExcel(Gstr1ReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("GSTR-1 Sales Report");

            // Fonts & Styles
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(sectionFont);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            decimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            CellStyle boldDecimalStyle = workbook.createCellStyle();
            boldDecimalStyle.setFont(boldFont);
            boldDecimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Title Block
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("GSTR-1 Outward Supplies (Sales) Report");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Period: " + report.getPeriodLabel());
            infoRow.getCell(0).setCellStyle(boldStyle);

            // Summary Section
            Row summaryTitleRow = sheet.createRow(3);
            summaryTitleRow.createCell(0).setCellValue("Summary Statistics");
            summaryTitleRow.getCell(0).setCellStyle(sectionStyle);

            String[] summaryHeaders = {"Metric", "Amount"};
            Row sumHeader = sheet.createRow(4);
            for (int i = 0; i < summaryHeaders.length; i++) {
                Cell cell = sumHeader.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Object[][] summaryData = {
                {"Total Taxable Value", report.getTotalTaxableValue()},
                {"Total CGST Amount", report.getTotalCgstAmount()},
                {"Total SGST Amount", report.getTotalSgstAmount()},
                {"Total IGST Amount", report.getTotalIgstAmount()},
                {"Total GST Tax Amount", report.getTotalGstAmount()},
                {"Total Invoice Value", report.getTotalInvoiceValue()}
            };

            int sumIdx = 5;
            for (Object[] data : summaryData) {
                Row row = sheet.createRow(sumIdx++);
                row.createCell(0).setCellValue((String) data[0]);
                Cell valCell = row.createCell(1);
                valCell.setCellValue(((BigDecimal) data[1]).doubleValue());
                valCell.setCellStyle(decimalStyle);
            }

            // Invoices Table
            int dataStartRow = sumIdx + 2;
            Row tableTitleRow = sheet.createRow(dataStartRow++);
            tableTitleRow.createCell(0).setCellValue("Sales Invoices Listing");
            tableTitleRow.getCell(0).setCellStyle(sectionStyle);

            String[] columns = {
                "Invoice No", "Invoice Date", "Customer Name", "Customer GSTIN", 
                "Taxable Value", "CGST Rate (%)", "CGST Amount", 
                "SGST Rate (%)", "SGST Amount", "IGST Rate (%)", "IGST Amount", 
                "Total Value", "Status"
            };

            Row tableHeaderRow = sheet.createRow(dataStartRow++);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Gstr1ItemDTO item : report.getItems()) {
                Row row = sheet.createRow(dataStartRow++);
                row.createCell(0).setCellValue(item.getInvoiceNo());
                row.createCell(1).setCellValue(item.getInvoiceDate().toString());
                row.createCell(2).setCellValue(item.getCustomerName());
                row.createCell(3).setCellValue(item.getCustomerGstin());
                
                Cell cTax = row.createCell(4);
                cTax.setCellValue(item.getTaxableValue().doubleValue());
                cTax.setCellStyle(decimalStyle);
                
                row.createCell(5).setCellValue(item.getCgstRate().doubleValue());
                
                Cell cCgst = row.createCell(6);
                cCgst.setCellValue(item.getCgstAmount().doubleValue());
                cCgst.setCellStyle(decimalStyle);
                
                row.createCell(7).setCellValue(item.getSgstRate().doubleValue());
                
                Cell cSgst = row.createCell(8);
                cSgst.setCellValue(item.getSgstAmount().doubleValue());
                cSgst.setCellStyle(decimalStyle);
                
                row.createCell(9).setCellValue(item.getIgstRate().doubleValue());
                
                Cell cIgst = row.createCell(10);
                cIgst.setCellValue(item.getIgstAmount().doubleValue());
                cIgst.setCellStyle(decimalStyle);
                
                Cell cTot = row.createCell(11);
                cTot.setCellValue(item.getTotalInvoiceValue().doubleValue());
                cTot.setCellStyle(decimalStyle);
                
                row.createCell(12).setCellValue(item.getStatus());
            }

            // Invoices Totals Row
            Row totalRow = sheet.createRow(dataStartRow);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTALS");
            totalLabel.setCellStyle(boldStyle);

            Cell totTax = totalRow.createCell(4);
            totTax.setCellValue(report.getTotalTaxableValue().doubleValue());
            totTax.setCellStyle(boldDecimalStyle);

            Cell totCgst = totalRow.createCell(6);
            totCgst.setCellValue(report.getTotalCgstAmount().doubleValue());
            totCgst.setCellStyle(boldDecimalStyle);

            Cell totSgst = totalRow.createCell(8);
            totSgst.setCellValue(report.getTotalSgstAmount().doubleValue());
            totSgst.setCellStyle(boldDecimalStyle);

            Cell totIgst = totalRow.createCell(10);
            totIgst.setCellValue(report.getTotalIgstAmount().doubleValue());
            totIgst.setCellStyle(boldDecimalStyle);

            Cell totVal = totalRow.createCell(11);
            totVal.setCellValue(report.getTotalInvoiceValue().doubleValue());
            totVal.setCellStyle(boldDecimalStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportGstr2ToExcel(Gstr2ReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("GSTR-2 Purchase Report");

            // Fonts & Styles
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(sectionFont);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            decimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            CellStyle boldDecimalStyle = workbook.createCellStyle();
            boldDecimalStyle.setFont(boldFont);
            boldDecimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Title Block
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("GSTR-2 Inward Supplies (Purchases) ITC Report");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Period: " + report.getPeriodLabel());
            infoRow.getCell(0).setCellStyle(boldStyle);

            // Summary Section
            Row summaryTitleRow = sheet.createRow(3);
            summaryTitleRow.createCell(0).setCellValue("Summary Statistics");
            summaryTitleRow.getCell(0).setCellStyle(sectionStyle);

            String[] summaryHeaders = {"Credit Type", "Amount"};
            Row sumHeader = sheet.createRow(4);
            for (int i = 0; i < summaryHeaders.length; i++) {
                Cell cell = sumHeader.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Object[][] summaryData = {
                {"Total Inward Taxable Value", report.getTotalTaxableValue()},
                {"Eligible CGST ITC", report.getTotalCgstAmount()},
                {"Eligible SGST ITC", report.getTotalSgstAmount()},
                {"Eligible IGST ITC", report.getTotalIgstAmount()},
                {"Total Eligible ITC (GST)", report.getTotalGstAmount()},
                {"Total Purchase Invoice Value", report.getTotalInvoiceValue()}
            };

            int sumIdx = 5;
            for (Object[] data : summaryData) {
                Row row = sheet.createRow(sumIdx++);
                row.createCell(0).setCellValue((String) data[0]);
                Cell valCell = row.createCell(1);
                valCell.setCellValue(((BigDecimal) data[1]).doubleValue());
                valCell.setCellStyle(decimalStyle);
            }

            // Invoices Table
            int dataStartRow = sumIdx + 2;
            Row tableTitleRow = sheet.createRow(dataStartRow++);
            tableTitleRow.createCell(0).setCellValue("Purchase Invoices & ITC Details");
            tableTitleRow.getCell(0).setCellStyle(sectionStyle);

            String[] columns = {
                "Internal No", "Supplier Invoice No", "Invoice Date", "Vendor Name", "Vendor GSTIN", 
                "Taxable Value", "CGST Amount", "SGST Amount", "IGST Amount", 
                "Total GST Paid", "Total Value", "Status"
            };

            Row tableHeaderRow = sheet.createRow(dataStartRow++);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Gstr2ItemDTO item : report.getItems()) {
                Row row = sheet.createRow(dataStartRow++);
                row.createCell(0).setCellValue(item.getInvoiceNumber());
                row.createCell(1).setCellValue(item.getSupplierInvoiceNumber());
                row.createCell(2).setCellValue(item.getInvoiceDate().toString());
                row.createCell(3).setCellValue(item.getVendorName());
                row.createCell(4).setCellValue(item.getVendorGstin());
                
                Cell cTax = row.createCell(5);
                cTax.setCellValue(item.getTaxableValue().doubleValue());
                cTax.setCellStyle(decimalStyle);
                
                Cell cCgst = row.createCell(6);
                cCgst.setCellValue(item.getCgstAmount().doubleValue());
                cCgst.setCellStyle(decimalStyle);
                
                Cell cSgst = row.createCell(7);
                cSgst.setCellValue(item.getSgstAmount().doubleValue());
                cSgst.setCellStyle(decimalStyle);
                
                Cell cIgst = row.createCell(8);
                cIgst.setCellValue(item.getIgstAmount().doubleValue());
                cIgst.setCellStyle(decimalStyle);
                
                Cell cGstVal = row.createCell(9);
                cGstVal.setCellValue(item.getTotalGstAmount().doubleValue());
                cGstVal.setCellStyle(decimalStyle);
                
                Cell cTot = row.createCell(10);
                cTot.setCellValue(item.getTotalInvoiceValue().doubleValue());
                cTot.setCellStyle(decimalStyle);
                
                row.createCell(11).setCellValue(item.getStatus());
            }

            // Totals Row
            Row totalRow = sheet.createRow(dataStartRow);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTALS");
            totalLabel.setCellStyle(boldStyle);

            Cell totTax = totalRow.createCell(5);
            totTax.setCellValue(report.getTotalTaxableValue().doubleValue());
            totTax.setCellStyle(boldDecimalStyle);

            Cell totCgst = totalRow.createCell(6);
            totCgst.setCellValue(report.getTotalCgstAmount().doubleValue());
            totCgst.setCellStyle(boldDecimalStyle);

            Cell totSgst = totalRow.createCell(7);
            totSgst.setCellValue(report.getTotalSgstAmount().doubleValue());
            totSgst.setCellStyle(boldDecimalStyle);

            Cell totIgst = totalRow.createCell(8);
            totIgst.setCellValue(report.getTotalIgstAmount().doubleValue());
            totIgst.setCellStyle(boldDecimalStyle);

            Cell totGst = totalRow.createCell(9);
            totGst.setCellValue(report.getTotalGstAmount().doubleValue());
            totGst.setCellStyle(boldDecimalStyle);

            Cell totVal = totalRow.createCell(10);
            totVal.setCellValue(report.getTotalInvoiceValue().doubleValue());
            totVal.setCellStyle(boldDecimalStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportGstr3bToExcel(Gstr3bReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("GSTR-3B Summary Report");

            // Fonts & Styles
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(sectionFont);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            decimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            CellStyle boldDecimalStyle = workbook.createCellStyle();
            boldDecimalStyle.setFont(boldFont);
            boldDecimalStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Title Block
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("GSTR-3B Monthly Return Summary Report");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Period: " + report.getPeriodLabel());
            infoRow.getCell(0).setCellStyle(boldStyle);

            // Outward Supplies (Tax Liability)
            int rowIdx = 3;
            Row outwardTitle = sheet.createRow(rowIdx++);
            outwardTitle.createCell(0).setCellValue("1. Details of Outward Supplies (Sales / Tax Liability)");
            outwardTitle.getCell(0).setCellStyle(sectionStyle);

            String[] tableHeaders = {"Supply Type / Nature of Tax", "Taxable Value", "CGST", "SGST", "IGST", "Total GST"};
            Row outHeader = sheet.createRow(rowIdx++);
            for (int i = 0; i < tableHeaders.length; i++) {
                Cell cell = outHeader.createCell(i);
                cell.setCellValue(tableHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Row outDataRow = sheet.createRow(rowIdx++);
            outDataRow.createCell(0).setCellValue("Outward Taxable Supplies (B2B / B2C)");
            
            Cell outTaxableVal = outDataRow.createCell(1);
            outTaxableVal.setCellValue(report.getOutwardTaxableValue().doubleValue());
            outTaxableVal.setCellStyle(decimalStyle);

            Cell outCgstVal = outDataRow.createCell(2);
            outCgstVal.setCellValue(report.getOutwardCgst().doubleValue());
            outCgstVal.setCellStyle(decimalStyle);

            Cell outSgstVal = outDataRow.createCell(3);
            outSgstVal.setCellValue(report.getOutwardSgst().doubleValue());
            outSgstVal.setCellStyle(decimalStyle);

            Cell outIgstVal = outDataRow.createCell(4);
            outIgstVal.setCellValue(report.getOutwardIgst().doubleValue());
            outIgstVal.setCellStyle(decimalStyle);

            Cell outTotVal = outDataRow.createCell(5);
            outTotVal.setCellValue(report.getTotalOutwardTax().doubleValue());
            outTotVal.setCellStyle(decimalStyle);

            // Inward Supplies (Input Tax Credit)
            rowIdx += 2;
            Row inwardTitle = sheet.createRow(rowIdx++);
            inwardTitle.createCell(0).setCellValue("2. Details of Eligible Input Tax Credit (ITC from Purchases)");
            inwardTitle.getCell(0).setCellStyle(sectionStyle);

            Row inHeader = sheet.createRow(rowIdx++);
            for (int i = 0; i < tableHeaders.length; i++) {
                Cell cell = inHeader.createCell(i);
                cell.setCellValue(tableHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Row inDataRow = sheet.createRow(rowIdx++);
            inDataRow.createCell(0).setCellValue("Inward Eligible ITC (Standard)");
            
            Cell inTaxableVal = inDataRow.createCell(1);
            inTaxableVal.setCellValue(report.getInwardTaxableValue().doubleValue());
            inTaxableVal.setCellStyle(decimalStyle);

            Cell inCgstVal = inDataRow.createCell(2);
            inCgstVal.setCellValue(report.getInwardCgst().doubleValue());
            inCgstVal.setCellStyle(decimalStyle);

            Cell inSgstVal = inDataRow.createCell(3);
            inSgstVal.setCellValue(report.getInwardSgst().doubleValue());
            inSgstVal.setCellStyle(decimalStyle);

            Cell inIgstVal = inDataRow.createCell(4);
            inIgstVal.setCellValue(report.getInwardIgst().doubleValue());
            inIgstVal.setCellStyle(decimalStyle);

            Cell inTotVal = inDataRow.createCell(5);
            inTotVal.setCellValue(report.getTotalInwardItc().doubleValue());
            inTotVal.setCellStyle(decimalStyle);

            // Net Tax Payable
            rowIdx += 2;
            Row netTitle = sheet.createRow(rowIdx++);
            netTitle.createCell(0).setCellValue("3. Net GST Tax Payable / (ITC Carry Forward Summary)");
            netTitle.getCell(0).setCellStyle(sectionStyle);

            String[] netHeaders = {"Tax Type", "Tax Liability", "Input Tax Credit (ITC)", "Net Payable (Paid in Cash)"};
            Row netHeaderRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < netHeaders.length; i++) {
                Cell cell = netHeaderRow.createCell(i);
                cell.setCellValue(netHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Object[][] netData = {
                {"CGST (Central Tax)", report.getOutwardCgst(), report.getInwardCgst(), report.getNetCgstPayable()},
                {"SGST (State Tax)", report.getOutwardSgst(), report.getInwardSgst(), report.getNetSgstPayable()},
                {"IGST (Integrated Tax)", report.getOutwardIgst(), report.getInwardIgst(), report.getNetIgstPayable()},
                {"TOTAL", report.getTotalOutwardTax(), report.getTotalInwardItc(), report.getTotalNetPayable()}
            };

            for (Object[] data : netData) {
                Row row = sheet.createRow(rowIdx++);
                boolean isTotal = data[0].equals("TOTAL");
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue((String) data[0]);
                labelCell.setCellStyle(isTotal ? boldStyle : null);

                for (int c = 1; c <= 3; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(((BigDecimal) data[c]).doubleValue());
                    cell.setCellStyle(isTotal ? boldDecimalStyle : decimalStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < tableHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
