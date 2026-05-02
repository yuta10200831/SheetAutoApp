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
    
    # シートのメタデータ（グループ情報など）を取得
    sheet = service.spreadsheets()
    result = sheet.get(spreadsheetId=spreadsheet_id, includeGridData=False).execute()
    
    for s in result.get('sheets', []):
        props = s.get('properties', {})
        print(f"\n=== Sheet: {props.get('title')} (ID: {props.get('sheetId')}) ===")
        
        # 行のグループ情報を表示
        row_groups = s.get('rowGroups', [])
        if row_groups:
            print("Row Groups:")
            for g in row_groups:
                r = g.get('range', {})
                print(f"  - Start: {r.get('startIndex')}, End: {r.get('endIndex')}, Depth: {g.get('depth')}, Collapsed: {g.get('collapsed', False)}")
        else:
            print("No row groups found.")
            
        # データも少し取得して表示（A列の日付など）
        try:
            data_result = sheet.values().get(spreadsheetId=spreadsheet_id, range=f"{props.get('title')}!A1:B20").execute()
            values = data_result.get('values', [])
            print("Sample Data (A1:B20):")
            for i, row in enumerate(values):
                print(f"  Row {i+1}: {row}")
        except Exception as e:
            print(f"  Could not read data: {e}")

if __name__ == '__main__':
    main()
