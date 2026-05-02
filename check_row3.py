import os
from google.oauth2 import service_account
from googleapiclient.discovery import build

creds_file = 'sheet-app/src/main/resources/credentials.json'
scopes = ['https://www.googleapis.com/auth/spreadsheets']
creds = service_account.Credentials.from_service_account_file(creds_file, scopes=scopes)
service = build('sheets', 'v4', credentials=creds)
spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'

sheet = service.spreadsheets()
data = sheet.values().get(spreadsheetId=spreadsheet_id, range="'学習時間を入れていく'!A3:ZZ3").execute()
print(data.get('values', [[]])[0])
