import os
from google.oauth2 import service_account
from googleapiclient.discovery import build

creds_file = 'sheet-app/src/main/resources/credentials.json'
scopes = ['https://www.googleapis.com/auth/spreadsheets']
creds = service_account.Credentials.from_service_account_file(creds_file, scopes=scopes)
service = build('sheets', 'v4', credentials=creds)
spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'

sheet = service.spreadsheets()
try:
    data = sheet.values().get(spreadsheetId=spreadsheet_id, range="'学習時間を入れていく'!A1:A20").execute()
    for i, row in enumerate(data.get('values', [])):
        print(f"Row {i+1}: {row}")
except Exception as e:
    print(e)
