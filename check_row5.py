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
    sheet_id = 147573482

    sheet_metadata = service.spreadsheets().get(spreadsheetId=spreadsheet_id, ranges=['学習時間を入れていく!A1:A10'], includeGridData=True).execute()
    sheets = sheet_metadata.get('sheets', '')
    
    for sheet in sheets:
        if sheet['properties']['sheetId'] == sheet_id:
            row_data = sheet.get('data', [])[0].get('rowMetadata', [])
            for i, row in enumerate(row_data):
                print(f"Row {i+1}: {row}")

if __name__ == '__main__':
    check_row()
