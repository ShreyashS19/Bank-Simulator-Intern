package com.bank.simulator.service;

import com.bank.simulator.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public ByteArrayOutputStream generateTransactionsExcel(List<Transaction> transactions) throws IOException {
        
        System.out.println("\n=== GENERATING EXCEL FILE ===");
        System.out.println("Total transactions to export: " + transactions.size());
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        CellStyle dateCellStyle = createDateCellStyle(workbook);
        
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        CellStyle centerStyle = createCenterAlignedStyle(workbook);
        
        CellStyle defaultStyle = createDefaultCellStyle(workbook);
        
        createHeaderRow(sheet, headerStyle);
        
        populateDataRows(sheet, transactions, dateCellStyle, currencyStyle, centerStyle, defaultStyle);
        
        autoSizeColumns(sheet, 6);
        
        sheet.createFreezePane(0, 1);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        System.out.println("Excel file generated successfully");
        System.out.println("File size: " + outputStream.size() + " bytes");
        
        return outputStream;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy hh:mm"));
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("₹#,##0.00"));
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.RIGHT);
        
        return style;
    }
    
    private CellStyle createCenterAlignedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        
        return style;
    }
    
   
    private CellStyle createDefaultCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        
        String[] headers = {
            "Sender Account", 
            "Receiver Account", 
            "Amount", 
            "Transaction Type", 
            "Description", 
            "Created Date"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        headerRow.setHeightInPoints(25);
    }
    
    private void populateDataRows(Sheet sheet, List<Transaction> transactions, 
                                  CellStyle dateCellStyle, CellStyle currencyStyle, 
                                  CellStyle centerStyle, CellStyle defaultStyle) {
        int rowNum = 1;
        
        for (Transaction transaction : transactions) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(transaction.getSenderAccountNumber());
            cell0.setCellStyle(defaultStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(transaction.getReceiverAccountNumber());
            cell1.setCellStyle(defaultStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(transaction.getAmount().doubleValue());
            cell2.setCellStyle(currencyStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(transaction.getTransactionType());
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            String description = transaction.getDescription();
            cell4.setCellValue(description != null && !description.trim().isEmpty() ? description : "N/A");
            cell4.setCellStyle(defaultStyle);
            
            Cell cell5 = row.createCell(5);
            String formattedDate = transaction.getCreatedDate().format(DATE_FORMATTER);
            cell5.setCellValue(formattedDate);
            cell5.setCellStyle(dateCellStyle);
        }
    }
    
    private void autoSizeColumns(Sheet sheet, int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
            //extra width for padding
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }
}
