import os
from google.oauth2 import service_account
from googleapiclient.discovery import build

creds_file = 'sheet-app/src/main/resources/credentials.json'
scopes = ['https://www.googleapis.com/auth/spreadsheets']
creds = service_account.Credentials.from_service_account_file(creds_file, scopes=scopes)
service = build('sheets', 'v4', credentials=creds)
spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'

# グループを削除するリクエスト
requests = [
    {
        "deleteDimensionGroup": {
            "range": {
                "sheetId": 147573482,
                "dimension": "COLUMNS",
                "startIndex": 124,
                "endIndex": 368
            }
        }
    },
    {
        "deleteDimensionGroup": {
            "range": {
                "sheetId": 898078840,
                "dimension": "ROWS",
                "startIndex": 123,
                "endIndex": 366
            }
        }
    }
]

try:
    service.spreadsheets().batchUpdate(
        spreadsheetId=spreadsheet_id,
        body={"requests": requests}
    ).execute()
    print("Successfully deleted the future groups!")
except Exception as e:
    print(f"Error: {e}")
