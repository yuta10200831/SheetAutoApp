import os
import sys
from google.oauth2 import service_account
from googleapiclient.discovery import build

def main():
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    creds_file = 'sheet-app/src/main/resources/credentials.json'
    
    scopes = ['https://www.googleapis.com/auth/spreadsheets']
    creds = service_account.Credentials.from_service_account_file(creds_file, scopes=scopes)
    service = build('sheets', 'v4', credentials=creds)
    
    # 全体シートのID
    sheet_id = 898078840
    
    # 既存のすべての行グループを削除するリクエスト
    requests = [
        {
            "deleteDimensionGroup": {
                "range": {
                    "sheetId": sheet_id,
                    "dimension": "ROWS",
                    "startIndex": 1,
                    "endIndex": 1000
                }
            }
        }
    ]
    
    body = {
        "requests": requests
    }
    
    try:
        service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
        print("✅ すべてのグループを削除してリセットしました！")
    except Exception as e:
        print(f"❌ エラー: {e}")

if __name__ == '__main__':
    main()
