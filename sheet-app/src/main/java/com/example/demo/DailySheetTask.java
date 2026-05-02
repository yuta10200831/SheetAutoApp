package com.example.demo;

import com.example.demo.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class DailySheetTask {

    private final GoogleSheetsService sheetsService;
    // 「全体」シートのID
    private static final int ZENTAI_SHEET_ID = 898078840;
    // 「学習時間を入れていく」シートのID
    private static final int GAKUSHU_SHEET_ID = 147573482;

    @Autowired
    public DailySheetTask(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    // GitHub Actionsから呼ばれるため、@Scheduledは削除
    public void executeDailyTask() {
        System.out.println("=========================================");
        System.out.println("定期実行タスクを開始します: " + LocalDate.now(ZoneId.of("Asia/Tokyo")));
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        
        System.out.println("「全体」シートのグループ表示を更新します。");
        updateDailyGroups(today, "全体!A:A", ZENTAI_SHEET_ID);
        
        System.out.println("「学習時間を入れていく」シートのグループ表示を更新します。");
        updateDailyGroups(today, "学習時間を入れていく!A:ZZ", GAKUSHU_SHEET_ID);

        System.out.println("定期実行タスクが完了しました。");
        System.out.println("=========================================");
    }
    
    /**
     * 指定した日付までのグループを作成・展開し、それ以外のグループを閉じる処理
     */
    private void updateDailyGroups(LocalDate targetDate, String range, int sheetId) {
        System.out.println("シートのデータを読み込んでいます... (" + range + ")");
        List<List<Object>> data = sheetsService.getSheetData(range);
        
        if (data == null || data.isEmpty()) {
            System.out.println("データが見つかりませんでした。");
            return;
        }

        // 探したい日付の文字列 (例: "5/2")
        String targetDateStr = targetDate.getMonthValue() + "/" + targetDate.getDayOfMonth();
        System.out.println("探す日付: " + targetDateStr);

        int targetIndex = -1;

        // シートごとにデータの構造が違うため、処理を分ける
        if (sheetId == GAKUSHU_SHEET_ID) {
            // 「学習時間を入れていく」シートの場合 (列と行のグループ化)
            // データ全体を取得している (A:ZZ)
            
            // --- 列のグループ化処理 ---
            if (data.size() > 2) {
                List<Object> row3 = data.get(2); // 3行目 (index 2)
                
                // 左から順に日付を探す
                for (int i = 0; i < row3.size(); i++) {
                    if (row3.get(i).toString().equals(targetDateStr)) {
                        targetIndex = i;
                        break;
                    }
                }
                
                if (targetIndex != -1) {
                    System.out.println("今日の日付は " + (targetIndex + 1) + " 列目に見つかりました。");
                    
                    // 既存の列グループをリセット
                    sheetsService.deleteGroups(sheetId, "COLUMNS", 2, row3.size()); // C列(index 2)から最後まで
                    
                    // C列(index 2)から、昨日の列(targetIndex)までをグループ化して閉じる
                    if (targetIndex > 2) {
                        sheetsService.addGroup(sheetId, "COLUMNS", 2, targetIndex);
                        sheetsService.updateGroupState(sheetId, "COLUMNS", 2, targetIndex, true);
                    }
                } else {
                    System.out.println("今日の日付が列に見つかりませんでした。");
                }
            }
            
            // --- 行のグループ化処理 ---
            // 日付が見つかった列(targetIndex)に基づいて、対応する行を計算する
            // データが斜めに入力されているため、行インデックス = 列インデックス + 2 になる
            // 例: 5/2 (列インデックス123) の場合、行インデックスは 125 (126行目)
            if (targetIndex != -1) {
                int targetRowIndex = targetIndex + 2;
                System.out.println("今日の日付に対応する行は " + (targetRowIndex + 1) + " 行目です。");
                
                // 既存の行グループをリセット
                sheetsService.deleteGroups(sheetId, "ROWS", 4, data.size() + 100); // 5行目(index 4)から最後まで (余裕を持たせる)
                
                // 5行目(index 4)から、昨日の行(targetRowIndex)までをグループ化して閉じる
                if (targetRowIndex > 4) {
                    sheetsService.addGroup(sheetId, "ROWS", 4, targetRowIndex);
                    sheetsService.updateGroupState(sheetId, "ROWS", 4, targetRowIndex, true);
                }
            } else {
                System.out.println("今日の日付が見つからなかったため、行のグループ化をスキップします。");
            }
            
        } else {
            // 「全体」シートの場合 (行のグループ化)
            // A列のデータを上から順に見ていく
            for (int i = 1; i < data.size(); i++) {
                List<Object> row = data.get(i);
                if (row.isEmpty()) continue;

                if (row.get(0).toString().equals(targetDateStr)) {
                    targetIndex = i;
                    break;
                }
            }
            
            if (targetIndex != -1) {
                System.out.println("今日の日付は " + (targetIndex + 1) + " 行目に見つかりました。");
                
                // 既存の行グループをリセット
                sheetsService.deleteGroups(sheetId, "ROWS", 1, data.size()); // 2行目(index 1)から最後まで
                
                // 2行目(index 1)から、昨日の行(targetIndex)までをグループ化して閉じる
                if (targetIndex > 1) {
                    sheetsService.addGroup(sheetId, "ROWS", 1, targetIndex);
                    sheetsService.updateGroupState(sheetId, "ROWS", 1, targetIndex, true);
                }
            } else {
                System.out.println("今日の日付が見つかりませんでした。");
            }
        }
    }
}
