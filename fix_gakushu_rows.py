import os
import pickle
from google.auth.transport.requests import Request
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']
CREDENTIALS_FILE = 'sheet-app/src/main/resources/credentials.json'

def get_credentials():
    creds = Credentials.from_service_account_file(CREDENTIALS_FILE, scopes=SCOPES)
    return creds

def fix_rows():
    creds = get_credentials()
    service = build('sheets', 'v4', credentials=creds)
    spreadsheet_id = '1lBOvlrPcuQa4DT7JKnqiTDr5gpr3BXWyVW3QiME-Ca4'
    sheet_id = 147573482

    # Get current groups
    sheet_metadata = service.spreadsheets().get(spreadsheetId=spreadsheet_id).execute()
    sheets = sheet_metadata.get('sheets', '')
    
    for sheet in sheets:
        if sheet['properties']['sheetId'] == sheet_id:
            row_groups = sheet.get('rowGroups', [])
            col_groups = sheet.get('columnGroups', [])
            
            requests = []
            
            # Delete all row groups
            for group in row_groups:
                requests.append({
                    "deleteDimensionGroup": {
                        "range": group['range']
                    }
                })
                
            # Delete all column groups
            for group in col_groups:
                requests.append({
                    "deleteDimensionGroup": {
                        "range": group['range']
                    }
                })
                
            if requests:
                print(f"Deleting {len(requests)} groups...")
                body = {'requests': requests}
                service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
                print("Deleted.")
            else:
                print("No groups found.")

if __name__ == '__main__':
    fix_rows()
