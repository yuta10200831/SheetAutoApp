import os
import sys
from google.oauth2 import service_account
from googleapiclient.discovery import build

def main():
    if len(sys.argv) < 2:
        print("Usage: python test_sheet.py <spreadsheet_id>")
        sys.exit(1)
        
    spreadsheet_id = sys.argv[1]
    creds_file = 'sheet-app/src/main/resources/credentials.json'
    
    if not os.path.exists(creds_file):
        print(f"Error: Credentials file {creds_file} not found.")
        sys.exit(1)
        
    print(f"Testing connection to Google Sheets API...")
    print(f"Spreadsheet ID: {spreadsheet_id}")
    
    try:
        # Define the scopes
        scopes = ['https://www.googleapis.com/auth/spreadsheets']
        
        # Load credentials
        creds = service_account.Credentials.from_service_account_file(
            creds_file, scopes=scopes)
            
        print(f"Service Account Email: {creds.service_account_email}")
            
        # Build the service
        service = build('sheets', 'v4', credentials=creds)
        
        # Call the Sheets API to get spreadsheet info
        sheet = service.spreadsheets()
        result = sheet.get(spreadsheetId=spreadsheet_id).execute()
        
        print("\n✅ SUCCESS! Successfully connected to the spreadsheet.")
        print(f"Spreadsheet Title: {result.get('properties', {}).get('title')}")
        print(f"Number of sheets: {len(result.get('sheets', []))}")
        
        print("\n--- Sheet Details ---")
        for s in result.get('sheets', []):
            props = s.get('properties', {})
            print(f"Title: '{props.get('title')}', ID: {props.get('sheetId')}")
        
    except Exception as e:
        print(f"\n❌ ERROR: Failed to access the spreadsheet.")
        print(f"Details: {str(e)}")
        print("\nPlease check:")
        print("1. Is the Spreadsheet ID correct?")
        print(f"2. Did you share the spreadsheet with {creds.service_account_email} and give it Editor access?")

if __name__ == '__main__':
    main()
