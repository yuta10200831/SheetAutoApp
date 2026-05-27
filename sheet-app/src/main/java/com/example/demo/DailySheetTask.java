package com.example.demo;

import com.example.demo.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class DailySheetTask {

    private final GoogleSheetsService sheetsService;
    private final LineNotificationService lineNotificationService; // 追加：LINE通知用サービス

    // 「全体」シートのID
    private static final int ZENTAI_SHEET_ID = 898078840;
    // 「学習時間を入れていく」シートのID
    private static final int GAKUSHU_SHEET_ID = 147573482;

    @Autowired
    public DailySheetTask(GoogleSheetsService sheetsService, LineNotificationService lineNotificationService) {
        this.sheetsService = sheetsService;
        this.lineNotificationService = lineNotificationService; // 追加：コンストラクタで受け取る
    }

    // GitHub Actionsから呼ばれるため、@Scheduledは削除
    public void executeDailyTask() {
        System.out.println("=========================================");
        System.out.println("定期実行タスクを開始します: " + LocalDate.now(ZoneId.of("Asia/Tokyo")));
        
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
            
            System.out.println("「全体」シートのグループ表示を更新します。");
            updateDailyGroups(today, "全体!A:A", ZENTAI_SHEET_ID);
            
            System.out.println("「学習時間を入れていく」シートのグループ表示を更新します。");
            updateDailyGroups(today, "学習時間を入れていく!A:ZZ", GAKUSHU_SHEET_ID);

            System.out.println("月別と今週の学習時間を集計して更新します。");
            updateSummaries();

            System.out.println("定期実行タスクが完了しました。");
            System.out.println("=========================================");

            // 成功した時のLINE通知！
            String successMessage = "✅ 今日のシート更新と学習時間の集計が完了したよ！\n今日もお疲れ様！☕️";
            lineNotificationService.sendLineMessage(successMessage);

        } catch (Exception e) {
            System.err.println("タスク実行中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();

            // エラーが起きた時のLINE通知！
            String errorMessage = "❌ シートの更新中にエラーが発生したみたい...\n詳細: " + e.getMessage();
            lineNotificationService.sendLineMessage(errorMessage);
            
            // GitHub Actionsにもエラーを伝えるために再スローする
            throw e;
        }
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
                    
                    // C列(index 2)から、一昨日の列(targetIndex - 1)までをグループ化して閉じる
                    int endColIndex = targetIndex - 1;
                    if (endColIndex > 2) {
                        sheetsService.addGroup(sheetId, "COLUMNS", 2, endColIndex);
                        sheetsService.updateGroupState(sheetId, "COLUMNS", 2, endColIndex, true);
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
                
                // 5行目(index 4)から、一昨日の行(targetRowIndex - 1)までをグループ化して閉じる
                int endRowIndex = targetRowIndex - 1;
                if (endRowIndex > 4) {
                    sheetsService.addGroup(sheetId, "ROWS", 4, endRowIndex);
                    sheetsService.updateGroupState(sheetId, "ROWS", 4, endRowIndex, true);
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
                
                // 2行目(index 1)から、一昨日の行(targetIndex - 1)までをグループ化して閉じる
                int endRowIndex = targetIndex - 1;
                if (endRowIndex > 1) {
                    sheetsService.addGroup(sheetId, "ROWS", 1, endRowIndex);
                    sheetsService.updateGroupState(sheetId, "ROWS", 1, endRowIndex, true);
                }
            } else {
                System.out.println("今日の日付が見つかりませんでした。");
            }
        }
    }

    /**
     * 「学習時間を入れていく」シートから日々の学習時間を読み取り、
     * 月別および今週の学習時間を集計して「時間集計シート」に書き込む処理
     */
    private void updateSummaries() {
        System.out.println("学習時間（月別・週間）の集計を開始します...");
        
        // 1. データの読み取り
        List<List<Object>> data = sheetsService.getSheetData("学習時間を入れていく!A:ZZ");
        if (data == null || data.size() <= 2) {
            System.out.println("集計するデータが見つかりませんでした。");
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        // 1月〜12月の合計を入れる配列（インデックス0が1月、11が12月）
        double[] monthlyTotals = new double[12];
        double weeklyTotal = 0;
        int currentYear = today.getYear();
        
        // 3行目（インデックス2）にある日付の列を取得
        List<Object> dateRow = data.get(2);

        // 2. 集計
        int count = 0;
        for (int col = 0; col < dateRow.size(); col++) {
            if (dateRow.get(col) == null) continue;
            String dateStr = dateRow.get(col).toString().trim();
            if (dateStr.isEmpty()) continue;

            String[] parts = dateStr.split("/");
            if (parts.length >= 2) {
                try {
                    int year = currentYear;
                    int month = -1;
                    int day = -1;

                    if (parts.length == 2) {
                        // "5/2" のパターン
                        month = Integer.parseInt(parts[0]);
                        day = Integer.parseInt(parts[1]);
                    } else if (parts.length == 3) {
                        // "2026/5/2" のパターン
                        year = Integer.parseInt(parts[0]);
                        month = Integer.parseInt(parts[1]);
                        day = Integer.parseInt(parts[2]);
                    }
                    
                    if (month >= 1 && month <= 12) {
                        // 学習時間が入っている行を計算（行インデックス = 列インデックス + 2）
                        int targetRow = col + 2;
                        
                        // データが存在する範囲内かチェック
                        if (targetRow < data.size()) {
                            List<Object> rowData = data.get(targetRow);
                            if (col < rowData.size() && rowData.get(col) != null) {
                                String timeStr = rowData.get(col).toString().trim();
                                if (!timeStr.isEmpty()) {
                                    try {
                                        double time = Double.parseDouble(timeStr);
                                        // 月別集計
                                        monthlyTotals[month - 1] += time;
                                        count++;
                                        
                                        // 週間集計
                                        try {
                                            LocalDate cellDate = LocalDate.of(year, month, day);
                                            if (!cellDate.isBefore(startOfWeek) && !cellDate.isAfter(endOfWeek)) {
                                                weeklyTotal += time;
                                            }
                                        } catch (Exception dateEx) {
                                            // 日付変換エラー時は週間集計のみスキップ
                                        }
                                        
                                    } catch (NumberFormatException e) {
                                        System.out.println("スキップ(時間エラー): 日付=" + dateStr + ", 時間=" + timeStr);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("スキップ(日付エラー): " + dateStr);
                }
            }
        }

        System.out.println("合計 " + count + " 件の学習時間データを集計しました。");
        System.out.println("今週の学習時間合計: " + weeklyTotal + "時間");

        // 3. 書き込み用のデータを作成（C2:C13に書き込むための縦長のリスト）
        List<List<Object>> monthlyWriteData = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            List<Object> row = new ArrayList<>();
            // 合計が0の場合は空白にしておく（グラフが綺麗になるように）
            if (monthlyTotals[i] > 0) {
                row.add(monthlyTotals[i]);
                System.out.println((i + 1) + "月の合計: " + monthlyTotals[i] + "時間");
            } else {
                row.add("");
            }
            monthlyWriteData.add(row);
        }

        // 4. 集計シートへ書き込み（月別）
        System.out.println("月別集計結果を「時間集計!C2:C13」に書き込みます...");
        sheetsService.writeData("時間集計!C2:C13", monthlyWriteData);
        
        // 5. 集計シートへ書き込み（週間）
        List<List<Object>> weeklyWriteData = new ArrayList<>();
        List<Object> weeklyRow = new ArrayList<>();
        weeklyRow.add(weeklyTotal > 0 ? weeklyTotal : "");
        weeklyWriteData.add(weeklyRow);
        
        System.out.println("週間集計結果を「時間集計!D2」に書き込みます...");
        sheetsService.writeData("時間集計!D2", weeklyWriteData);

        System.out.println("集計処理がすべて完了しました！");
    }
}
