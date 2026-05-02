import os
import pickle
from google.auth.transport.requests import Request
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from datetime import datetime

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']
CREDENTIALS_FILE = 'sheet-app/src/main/resources/credentials.json'

def get_credentials():
    creds = Credentials.from_service_account_file(CREDENTIALS_FILE, scopes=SCOPES)
    return creds

def test_run():
    creds = get_credentials()
    service = build('sheets', 'v4', credentials=creds)
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    sheet_id = 147573482
    
    today = datetime.now()
    target_date_str = f"{today.month}/{today.day}"
    today_day_str = str(today.day)
    
    print(f"Target date: {target_date_str}, Target day: {today_day_str}")

    # Get data
    result = service.spreadsheets().values().get(
        spreadsheetId=spreadsheet_id,
        range='学習時間を入れていく!A:ZZ'
    ).execute()
    data = result.get('values', [])
    
    if not data:
        print("No data found.")
        return
        
    print(f"Got {len(data)} rows.")
    
    # Column grouping logic
    target_col_index = -1
    if len(data) > 2:
        row3 = data[2]
        print(f"Row 3 has {len(row3)} columns. First 5: {row3[:5]}")
        for i, val in enumerate(row3):
            if val == target_date_str:
                target_col_index = i
                break
                
    if target_col_index != -1:
        print(f"Found date at column index {target_col_index} (Column {chr(65+target_col_index)})")
    else:
        print("Date not found in row 3")
        
    # Row grouping logic
    target_row_index = -1
    for i in range(4, len(data)):
        row = data[i]
        if not row:
            continue
        if row[0] == today_day_str:
            target_row_index = i
            break
            
    if target_row_index != -1:
        print(f"Found day at row index {target_row_index} (Row {target_row_index + 1})")
    else:
        print("Day not found in column A starting from row 5")

if __name__ == '__main__':
    test_run()
