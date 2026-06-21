package com.example.demo;

import com.example.demo.service.GoogleSheetsService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

// @Component
public class SheetNamePrinter implements CommandLineRunner {

    private final GoogleSheetsService sheetsService;

    @Autowired
    public SheetNamePrinter(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== シート一覧を取得します ===");
        try {
            Method getSheetsServiceMethod = GoogleSheetsService.class.getDeclaredMethod("getSheetsService");
            getSheetsServiceMethod.setAccessible(true);
            Sheets service = (Sheets) getSheetsServiceMethod.invoke(sheetsService);
            
            Spreadsheet spreadsheet = service.spreadsheets().get("1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4").execute();
            List<Sheet> sheets = spreadsheet.getSheets();
            for (Sheet sheet : sheets) {
                System.out.println("ID: " + sheet.getProperties().getSheetId() + ", Name: " + sheet.getProperties().getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=============================");
        System.exit(0);
    }
}
