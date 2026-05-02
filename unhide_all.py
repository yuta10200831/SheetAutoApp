import os
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']
CREDENTIALS_FILE = 'sheet-app/src/main/resources/credentials.json'

def get_credentials():
    creds = Credentials.from_service_account_file(CREDENTIALS_FILE, scopes=SCOPES)
    return creds

def unhide_all():
    creds = get_credentials()
    service = build('sheets', 'v4', credentials=creds)
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    sheet_id = 147573482

    requests = []
    
    # Unhide rows
    requests.append({
        "updateDimensionProperties": {
            "range": {
                "sheetId": sheet_id,
                "dimension": "ROWS",
                "startIndex": 4,
                "endIndex": 400
            },
            "properties": {
                "hiddenByUser": False
            },
            "fields": "hiddenByUser"
        }
    })
    
    # Unhide columns
    requests.append({
        "updateDimensionProperties": {
            "range": {
                "sheetId": sheet_id,
                "dimension": "COLUMNS",
                "startIndex": 2,
                "endIndex": 400
            },
            "properties": {
                "hiddenByUser": False
            },
            "fields": "hiddenByUser"
        }
    })

    print("Unhiding rows and columns...")
    body = {'requests': requests}
    service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
    print("Done.")

if __name__ == '__main__':
    unhide_all()
