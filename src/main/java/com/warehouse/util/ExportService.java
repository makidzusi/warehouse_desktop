package com.warehouse.util;

import com.warehouse.model.Product;
import com.warehouse.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void exportProducts(List<Product> products, String filePath) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Товары");

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);

            String[] headers = {"Артикул", "Наименование", "Категория", "Остаток", "Ед.", "Мин.", "Цена (₽)", "Статус"};
            createHeaderRow(sheet, headers, headerStyle);

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.getSku());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
                row.createCell(3).setCellValue(p.getQuantity());
                row.createCell(4).setCellValue(p.getUnit() != null ? p.getUnit() : "");
                row.createCell(5).setCellValue(p.getMinQuantity());

                Cell priceCell = row.createCell(6);
                if (p.getPrice() != null) {
                    priceCell.setCellValue(p.getPrice().doubleValue());
                    priceCell.setCellStyle(moneyStyle);
                }

                row.createCell(7).setCellValue(p.isLowStock() ? "Мало на складе" : "В норме");
            }

            autoSize(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
    }

    public void exportTransactions(List<Transaction> transactions, String filePath) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Движение товаров");

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);

            String[] headers = {"Дата", "Тип", "Товар", "Кол-во", "Цена/ед. (₽)", "Сумма (₽)", "Контрагент", "Пользователь", "Примечание"};
            createHeaderRow(sheet, headers, headerStyle);

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(
                    t.getCreatedAt() != null ? t.getCreatedAt().format(DTF) : "");
                row.createCell(1).setCellValue(t.getType().getDisplayName());
                row.createCell(2).setCellValue(t.getProductName() != null ? t.getProductName() : "");
                row.createCell(3).setCellValue(t.getQuantity());

                Cell unitPriceCell = row.createCell(4);
                if (t.getUnitPrice() != null) {
                    unitPriceCell.setCellValue(t.getUnitPrice().doubleValue());
                    unitPriceCell.setCellStyle(moneyStyle);
                }

                Cell totalCell = row.createCell(5);
                if (t.getTotalPrice() != null) {
                    totalCell.setCellValue(t.getTotalPrice().doubleValue());
                    totalCell.setCellStyle(moneyStyle);
                }

                String counterparty = t.getType() == Transaction.Type.INCOMING
                    ? t.getSupplier() : t.getCustomer();
                row.createCell(6).setCellValue(counterparty != null ? counterparty : "");
                row.createCell(7).setCellValue(t.getUserName() != null ? t.getUserName() : "");
                row.createCell(8).setCellValue(t.getNote() != null ? t.getNote() : "");
            }

            autoSize(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // add small padding
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }
    }
}
