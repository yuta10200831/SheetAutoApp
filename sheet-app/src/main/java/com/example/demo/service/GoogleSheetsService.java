package com.example.demo.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.DimensionGroup;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UpdateDimensionGroupRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.DimensionGroup;
import com.google.api.services.sheets.v4.model.UpdateDimensionGroupRequest;
import com.google.api.services.sheets.v4.model.DimensionGroup;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Sheet Auto App";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SPREADSHEET_ID = "1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4"; // 先ほど確認したID

    /**
     * Sheets APIのサービスクライアントを構築する
     */
    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        // resourcesフォルダにある credentials.json を読み込む
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 指定した範囲のデータを取得する
     * @param range 取得したい範囲 (例: "Sheet1!A1:C10")
     * @return データの2次元リスト
     */
    public List<List<Object>> getSheetData(String range) {
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
            
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
                return Collections.emptyList();
            }
            return values;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 指定した範囲にデータを書き込む
     * @param range 書き込む範囲 (例: "シート1!A1")
     * @param values 書き込むデータの2次元リスト
     */
    public void writeData(String range, List<List<Object>> values) {
        try {
            Sheets service = getSheetsService();
            ValueRange body = new ValueRange().setValues(values);
            
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, body)
                    .setValueInputOption("USER_ENTERED") // ユーザーが入力したのと同じように解釈(数式もOK)
                    .execute();
                    
            System.out.println(result.getUpdatedCells() + " 個のセルを更新しました。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定したセルの背景色と文字を太字にする（条件付き書式のような見た目の変更）
     * @param sheetId シートのID (URLのgid=の後ろの数字。最初のシートは通常0)
     * @param startRow 開始行 (0始まり)
     * @param endRow 終了行
     * @param startCol 開始列 (0始まり, A=0, B=1...)
     * @param endCol 終了列
     */
    public void formatHeaderCells(Integer sheetId, int startRow, int endRow, int startCol, int endCol) {
        try {
            Sheets service = getSheetsService();
            
            // 背景色を薄い青色に設定
            Color backgroundColor = new Color()
                    .setRed(0.8f)
                    .setGreen(0.9f)
                    .setBlue(1.0f);
                    
            // 文字を太字に設定
            TextFormat textFormat = new TextFormat().setBold(true);
            
            CellFormat cellFormat = new CellFormat()
                    .setBackgroundColor(backgroundColor)
                    .setTextFormat(textFormat);
                    
            CellData cellData = new CellData().setUserEnteredFormat(cellFormat);
            
            // 変更する範囲を指定
            GridCoordinate startCoordinate = new GridCoordinate()
                    .setSheetId(sheetId)
                    .setRowIndex(startRow)
                    .setColumnIndex(startCol);
                    
            RepeatCellRequest repeatCellRequest = new RepeatCellRequest()
                    .setRange(new com.google.api.services.sheets.v4.model.GridRange()
                            .setSheetId(sheetId)
                            .setStartRowIndex(startRow)
                            .setEndRowIndex(endRow)
                            .setStartColumnIndex(startCol)
                            .setEndColumnIndex(endCol))
                    .setCell(cellData)
                    .setFields("userEnteredFormat(backgroundColor,textFormat)");
                    
            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setRepeatCell(repeatCellRequest));
            
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);
                    
            service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
            System.out.println("セルの書式を更新しました。");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定した範囲のグループを開く、または閉じる
     * @param sheetId シートのID
     * @param dimension "ROWS" または "COLUMNS"
     * @param startIndex グループの開始インデックス (0始まり)
     * @param endIndex グループの終了インデックス (このインデックスは含まれない)
     * @param collapsed trueなら閉じる（折りたたむ）、falseなら開く（展開する）
     */
    public void updateGroupState(Integer sheetId, String dimension, int startIndex, int endIndex, boolean collapsed) {
        try {
            Sheets service = getSheetsService();

            DimensionRange range = new DimensionRange()
                    .setSheetId(sheetId)
                    .setDimension(dimension)
                    .setStartIndex(startIndex)
                    .setEndIndex(endIndex);

            DimensionGroup group = new DimensionGroup()
                    .setRange(range)
                    .setDepth(1) // グループの深さを指定（1階層目）
                    .setCollapsed(collapsed);

            UpdateDimensionGroupRequest updateGroupRequest = new UpdateDimensionGroupRequest()
                    .setDimensionGroup(group)
                    .setFields("collapsed");

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setUpdateDimensionGroup(updateGroupRequest));

            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
            System.out.println(dimension + " " + (startIndex + 1) + " から " + endIndex + " までのグループを " + (collapsed ? "閉じました" : "開きました"));

        } catch (Exception e) {
            System.err.println("グループの更新に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 指定した範囲の既存のグループをすべて削除し、非表示状態を解除する
     */
    public void deleteGroups(Integer sheetId, String dimension, int startIndex, int endIndex) {
        try {
            Sheets service = getSheetsService();
            List<Request> requests = new ArrayList<>();

            // 1. グループの削除リクエスト
            com.google.api.services.sheets.v4.model.DeleteDimensionGroupRequest deleteGroupRequest = 
                    new com.google.api.services.sheets.v4.model.DeleteDimensionGroupRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension(dimension)
                            .setStartIndex(startIndex)
                            .setEndIndex(endIndex));
            requests.add(new Request().setDeleteDimensionGroup(deleteGroupRequest));

            // 2. 非表示状態の解除（再表示）リクエスト
            com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest unhideRequest =
                    new com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension(dimension)
                            .setStartIndex(startIndex)
                            .setEndIndex(endIndex))
                    .setProperties(new com.google.api.services.sheets.v4.model.DimensionProperties()
                            .setHiddenByUser(false))
                    .setFields("hiddenByUser");
            requests.add(new Request().setUpdateDimensionProperties(unhideRequest));

            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
            System.out.println("既存の " + dimension + " グループを削除し、再表示しました。");

        } catch (Exception e) {
            // グループが存在しない場合でも、非表示解除だけは実行したいので、エラーをキャッチして非表示解除のみ再試行する
            try {
                Sheets service = getSheetsService();
                List<Request> requests = new ArrayList<>();
                com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest unhideRequest =
                        new com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension(dimension)
                                .setStartIndex(startIndex)
                                .setEndIndex(endIndex))
                        .setProperties(new com.google.api.services.sheets.v4.model.DimensionProperties()
                                .setHiddenByUser(false))
                        .setFields("hiddenByUser");
                requests.add(new Request().setUpdateDimensionProperties(unhideRequest));
                
                BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                        .setRequests(requests);
                service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
                System.out.println(dimension + " の非表示状態を解除しました（グループは存在しませんでした）。");
            } catch (Exception ex) {
                System.err.println("非表示解除に失敗しました: " + ex.getMessage());
            }
        }
    }

    /**
     * 新しいグループを作成する
     */
    public void addGroup(Integer sheetId, String dimension, int startIndex, int endIndex) {
        try {
            Sheets service = getSheetsService();

            DimensionRange range = new DimensionRange()
                    .setSheetId(sheetId)
                    .setDimension(dimension)
                    .setStartIndex(startIndex)
                    .setEndIndex(endIndex);

            com.google.api.services.sheets.v4.model.AddDimensionGroupRequest addGroupRequest = 
                    new com.google.api.services.sheets.v4.model.AddDimensionGroupRequest()
                    .setRange(range);

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setAddDimensionGroup(addGroupRequest));

            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
            System.out.println(dimension + " " + (startIndex + 1) + " から " + endIndex + " に新しいグループを作成しました");

        } catch (Exception e) {
            System.err.println("グループの作成に失敗しました: " + e.getMessage());
        }
    }
}
