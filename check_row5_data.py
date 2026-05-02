import os
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']
CREDENTIALS_FILE = 'sheet-app/src/main/resources/credentials.json'

def get_credentials():
    creds = Credentials.from_service_account_file(CREDENTIALS_FILE, scopes=SCOPES)
    return creds

def check_row():
    creds = get_credentials()
    service = build('sheets', 'v4', credentials=creds)
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    
    result = service.spreadsheets().values().get(
        spreadsheetId=spreadsheet_id,
        range='学習時間を入れていく!A5:Z5'
    ).execute()
    data = result.get('values', [])
    print(f"Row 5 data: {data}")

if __name__ == '__main__':
    check_row()
