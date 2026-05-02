import os
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
            
            requests = []
            
            # Delete all row groups
            for group in row_groups:
                requests.append({
                    "deleteDimensionGroup": {
                        "range": group['range']
                    }
                })
                
            if requests:
                body = {'requests': requests}
                service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
                print("Deleted old row groups.")
            
            requests = []
            
            # Unhide all rows first just in case
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
            body = {'requests': requests}
            service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
            
            requests = []
            # Add new row group from row 5 (index 4) to row 125 (index 124)
            # Today is May 2nd, column index 123. Today's row is 123 + 2 = 125.
            # So we group up to index 124 (which is row 125).
            requests.append({
                "addDimensionGroup": {
                    "range": {
                        "sheetId": sheet_id,
                        "dimension": "ROWS",
                        "startIndex": 4,
                        "endIndex": 125
                    }
                }
            })
            
            body = {'requests': requests}
            service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
            print("Added new row group.")
                
            requests = []
            requests.append({
                "updateDimensionGroup": {
                    "dimensionGroup": {
                        "range": {
                            "sheetId": sheet_id,
                            "dimension": "ROWS",
                            "startIndex": 4,
                            "endIndex": 125
                        },
                        "depth": 1,
                        "collapsed": True
                    },
                    "fields": "collapsed"
                }
            })
            
            body = {'requests': requests}
            service.spreadsheets().batchUpdate(spreadsheetId=spreadsheet_id, body=body).execute()
            print("Collapsed new row group.")

if __name__ == '__main__':
    fix_rows()
