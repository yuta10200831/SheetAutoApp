package com.example.demo;

import com.example.demo.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SheetAutoRunner implements CommandLineRunner {

    private final GoogleSheetsService sheetsService;
    private final DailySheetTask dailySheetTask;

    @Autowired
    public SheetAutoRunner(GoogleSheetsService sheetsService, DailySheetTask dailySheetTask) {
        this.sheetsService = sheetsService;
        this.dailySheetTask = dailySheetTask;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================");
        System.out.println("Spring Boot アプリケーションが起動しました！");
        System.out.println("GitHub Actionsからの実行のため、即座にタスクを開始します。");
        System.out.println("=========================================");
        
        // 起動時にすぐ実行する
        dailySheetTask.executeDailyTask();
        
        System.out.println("すべての処理が完了しました。アプリケーションを終了します。");
        // 処理が終わったらアプリケーションを終了する
        System.exit(0);
    }
}
