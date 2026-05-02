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
        range='学習時間を入れていく!A1:C130'
    ).execute()
    data = result.get('values', [])
    for i, row in enumerate(data[-20:]):
        print(f"Row {len(data)-20+i+1}: {row}")

if __name__ == '__main__':
    check_data()
