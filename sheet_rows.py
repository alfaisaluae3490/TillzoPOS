import openpyxl
fn = r"C:\Users\Faisal Khan\Downloads\Faisal Mart — TillzoPOS (1).xlsx"
wb = openpyxl.load_workbook(fn, read_only=True, data_only=True)
for tab in ["Users_Permissions","Customers","Vendors","Expenses","Time_Clock","Inventory","Sales_Aug_2026","Wastage_Ledger","Stock_Adjustments","Till_Sessions","Purchase_Orders","GRN_Headers","Returns","Khata_Events","Categories"]:
    ws = wb[tab]
    rows = [r for r in ws.iter_rows(values_only=True) if any(c is not None and str(c).strip()!='' for c in r)]
    print(f"\n=== {tab}: {len(rows)} rows ===")
    for r in rows[:6]:
        print(" | ".join((str(c)[:26] if c is not None else '·') for c in r[:10]))
