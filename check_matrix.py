import os
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']
CREDENTIALS_FILE = 'sheet-app/src/main/resources/credentials.json'

def get_credentials():
    creds = Credentials.from_service_account_file(CREDENTIALS_FILE, scopes=SCOPES)
    return creds

def check_data():
    creds = get_credentials()
    service = build('sheets', 'v4', credentials=creds)
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    
    result = service.spreadsheets().values().get(
        spreadsheetId=spreadsheet_id,
        range='学習時間を入れていく!A1:ZZ130'
    ).execute()
    data = result.get('values', [])
    
    # Find where the data is
    for i, row in enumerate(data):
        if i < 4: continue # skip headers
        for j, cell in enumerate(row):
            if j > 1 and cell.strip() and cell.strip() != '0':
                print(f"Row {i+1}, Col {j+1} (index {j}): {cell}")

if __name__ == '__main__':
    check_data()
