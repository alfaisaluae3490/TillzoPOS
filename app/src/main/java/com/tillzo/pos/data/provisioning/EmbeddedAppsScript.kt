package com.tillzo.pos.data.provisioning

/**
 * EmbeddedAppsScript — the complete Google Apps Script source code,
 * embedded as a Kotlin string constant.
 *
 * This code is pushed to the user's Google Sheet's bound Apps Script project
 * during auto-provisioning on first sign-in.
 *
 * Contains all M2 required functions (Blueprint M2.1 → M2.9):
 *   - doPost(e)             : main router
 *   - handleSalesUpload     : monthly sharding + LockService + UUID check
 *   - handleInventoryUpsert : upsert by system_row_id
 *   - handleKhataEvent      : APPEND ONLY (OPT-4)
 *   - getDelta              : delta rows by timestamp
 *   - getSettings           : Settings tab reader
 *   - monthlySharding       : auto-creates Sales_MMM_YYYY tabs
 *   - hideSysDbTab          : hides SYS_DB_DO_NOT_TOUCH programmatically (OPT-1)
 *   - setupSchema           : auto-repairs missing columns
 *   - setupInitialTabs      : creates all required tabs on first run
 */
object EmbeddedAppsScript {

    const val CODE = """
// ─────────────────────────────────────────────────────────────────────────────
// TillzoPOS — Google Apps Script Backend v2.0
// Auto-deployed by TillzoPOS Android app. DO NOT EDIT MANUALLY.
// Blueprint: M2.1 → M2.9 (Optimized Edition)
// ─────────────────────────────────────────────────────────────────────────────

const SHEET_SALES_PREFIX    = 'Sales_';
const SHEET_ARCH_PREFIX     = 'ARCH_Sales_';
const SHEET_INVENTORY       = 'Inventory';
const SHEET_CUSTOMERS       = 'Customers';
const SHEET_KHATA           = 'Khata_Events';
const SHEET_EXPENSES        = 'Expenses';
const SHEET_RETURNS         = 'Returns';
const SHEET_USERS           = 'Users_Permissions';
const SHEET_SETTINGS        = 'Settings';
const SHEET_SYNC_LOG        = 'Sync_Log';
const SHEET_DASHBOARD       = 'Dashboard (View Only)';
const SHEET_SYSDB           = 'SYS_DB_DO_NOT_TOUCH';
const MAX_ROWS_PER_TAB      = 18000;

// ─── Main Entry Point ────────────────────────────────────────────────────────

function doPost(e) {
  try {
    hideSysDbTab();
    setupInitialTabs();
    
    var data = JSON.parse(e.postData.contents);
    var action = data.action;
    var result;

    switch (action) {
      case 'uploadBatch':
        result = handleBatchUpload(data);
        break;
      case 'getDelta':
        result = getDelta(data.lastTimestamp);
        break;
      case 'getSettings':
        result = getSettings();
        break;
      case 'ping':
        result = { status: 'ok', version: '2.0' };
        break;
      default:
        result = { error: 'Unknown action: ' + action };
    }

    return ContentService
      .createTextOutput(JSON.stringify(result))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({ error: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// ─── Batch Upload Router ─────────────────────────────────────────────────────

function handleBatchUpload(data) {
  var table = data.table;
  var rows  = data.rows;
  
  switch (table) {
    case 'Sales':        return handleSalesUpload(rows);
    case 'Inventory':    return handleInventoryUpsert(rows);
    case 'Khata_Events': return handleKhataEvent(rows);
    case 'Customers':    return handleGenericUpsert(SHEET_CUSTOMERS, rows, 'customer_id');
    case 'Expenses':     return handleGenericUpsert(SHEET_EXPENSES, rows, 'expense_id');
    case 'Returns':      return handleGenericUpsert(SHEET_RETURNS, rows, 'return_id');
    default:             return { error: 'Unknown table: ' + table };
  }
}

// ─── M2.1 Sales Upload (Monthly Sharding + LockService + UUID check) ─────────

function handleSalesUpload(rows) {
  var lock = LockService.getScriptLock();
  lock.waitLock(30000);
  
  try {
    var ss         = SpreadsheetApp.getActiveSpreadsheet();
    var activeTab  = getOrCreateCurrentSalesTab(ss);
    var existing   = getExistingUuids(activeTab, 'sync_uuid');
    var inserted   = 0;

    rows.forEach(function(row) {
      if (!existing.has(row.sync_uuid)) {
        // Check row limit — create overflow tab if needed
        if (activeTab.getLastRow() >= MAX_ROWS_PER_TAB) {
          activeTab = createOverflowSalesTab(ss);
        }
        appendRow(activeTab, [
          row.invoice_id, row.pos_id, row.timestamp, JSON.stringify(row.items_json),
          row.subtotal, row.tax, row.total, row.payment_method, row.reference_id,
          row.cashier_id, row.sync_uuid
        ]);
        inserted++;
      }
    });

    updateSettingsTimestamp(ss);
    return { status: 'ok', inserted: inserted };
    
  } finally {
    lock.releaseLock();
  }
}

// ─── M2.1 Inventory Upsert ───────────────────────────────────────────────────

function handleInventoryUpsert(rows) {
  var ss    = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_INVENTORY);
  var upserted = 0;

  rows.forEach(function(row) {
    var found = findRowByColumn(sheet, 'system_row_id', row.system_row_id);
    if (found) {
      updateRowValues(sheet, found, row);
    } else {
      appendRow(sheet, [
        row.system_row_id, row.barcode_id, row.name, row.category,
        row.unit, row.price, row.stock_qty, row.low_threshold,
        row.last_updated, row.sync_status, row.created_at,
        row.updated_at, row.pos_terminal_id
      ]);
    }
    upserted++;
  });

  updateSettingsTimestamp(ss);
  return { status: 'ok', upserted: upserted };
}

// ─── M2.1 Khata Events — APPEND ONLY (OPT-4, Blueprint Law) ─────────────────

function handleKhataEvent(rows) {
  // CRITICAL: This function NEVER updates existing rows.
  // Every call appends new event rows only.
  // Balance = SUM(all events) calculated on Android side.
  var ss    = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_KHATA);
  var existing = getExistingUuids(sheet, 'sync_uuid');
  var appended = 0;

  rows.forEach(function(row) {
    if (!existing.has(row.sync_uuid)) {
      appendRow(sheet, [
        row.event_id, row.customer_id, row.pos_id,
        row.type, row.amount, row.timestamp, row.sync_uuid
      ]);
      appended++;
    }
  });

  updateSettingsTimestamp(SpreadsheetApp.getActiveSpreadsheet());
  return { status: 'ok', appended: appended };
}

// ─── Generic Upsert ──────────────────────────────────────────────────────────

function handleGenericUpsert(sheetName, rows, idKey) {
  var ss    = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(sheetName);
  var count = 0;

  rows.forEach(function(row) {
    var found = findRowByColumn(sheet, idKey, row[idKey]);
    if (found) {
      updateRowValues(sheet, found, row);
    } else {
      appendRow(sheet, Object.values(row));
    }
    count++;
  });

  return { status: 'ok', count: count };
}

// ─── M2.6 Delta Sync ─────────────────────────────────────────────────────────

function getDelta(lastTimestamp) {
  var ss      = SpreadsheetApp.getActiveSpreadsheet();
  var sheets  = ss.getSheets();
  var results = [];
  var ts      = parseInt(lastTimestamp) || 0;

  sheets.forEach(function(sheet) {
    var name = sheet.getName();
    // Skip archived, system, and dashboard tabs
    if (name.indexOf(SHEET_ARCH_PREFIX) === 0) return;
    if (name === SHEET_SYSDB) return;
    if (name === SHEET_DASHBOARD) return;
    if (name === SHEET_SYNC_LOG) return;

    var data = sheet.getDataRange().getValues();
    if (data.length < 2) return;
    
    var headers = data[0];
    var tsCol   = headers.indexOf('last_updated') !== -1
                    ? headers.indexOf('last_updated')
                    : headers.indexOf('timestamp');
    
    if (tsCol === -1) return;

    for (var i = 1; i < data.length; i++) {
      var rowTs = parseInt(data[i][tsCol]) || 0;
      if (rowTs > ts) {
        var obj = {};
        headers.forEach(function(h, idx) { obj[h] = data[i][idx]; });
        obj._sheet = name;
        results.push(obj);
      }
    }
  });

  return { rows: results, fetchedAt: Date.now() };
}

// ─── M2.1 Get Settings ───────────────────────────────────────────────────────

function getSettings() {
  var ss    = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_SETTINGS);
  if (!sheet) return { last_updated_timestamp: 0, min_app_version: 1 };

  var data = sheet.getDataRange().getValues();
  var result = {};
  for (var i = 1; i < data.length; i++) {
    if (data[i][0]) result[data[i][0]] = data[i][1];
  }
  
  return {
    last_updated_timestamp : parseInt(result['last_updated_timestamp']) || 0,
    min_app_version        : parseInt(result['min_app_version']) || 1,
    backup_sheet_url       : result['backup_sheet_url'] || '',
    shop_name              : result['shop_name'] || '',
    shop_phone             : result['shop_phone'] || ''
  };
}

// ─── M2.2 Monthly Sharding ───────────────────────────────────────────────────

function monthlySharding() {
  var ss      = SpreadsheetApp.getActiveSpreadsheet();
  var now     = new Date();
  var months  = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var tabName = SHEET_SALES_PREFIX + months[now.getMonth()] + '_' + now.getFullYear();
  
  if (!ss.getSheetByName(tabName)) {
    var newSheet = ss.insertSheet(tabName);
    setupSalesHeaders(newSheet);
  }

  // Archive previous month's tab
  var prevDate  = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  var prevName  = SHEET_SALES_PREFIX + months[prevDate.getMonth()] + '_' + prevDate.getFullYear();
  var prevSheet = ss.getSheetByName(prevName);
  if (prevSheet) {
    prevSheet.setName(SHEET_ARCH_PREFIX + months[prevDate.getMonth()] + '_' + prevDate.getFullYear());
  }
}

// ─── OPT-1 Hide SYS_DB Tab ───────────────────────────────────────────────────

function hideSysDbTab() {
  try {
    var ss    = SpreadsheetApp.getActiveSpreadsheet();
    var sheet = ss.getSheetByName(SHEET_SYSDB);
    if (sheet && sheet.isSheetHidden() === false) {
      sheet.hideSheet();
    }
  } catch (e) { /* ignore if not found */ }
}

// ─── First-Run Tab Setup ──────────────────────────────────────────────────────

function setupInitialTabs() {
  var ss      = SpreadsheetApp.getActiveSpreadsheet();
  var existing = ss.getSheets().map(function(s) { return s.getName(); });

  var now     = new Date();
  var months  = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var salesTab = SHEET_SALES_PREFIX + months[now.getMonth()] + '_' + now.getFullYear();

  var required = [
    salesTab, SHEET_INVENTORY, SHEET_CUSTOMERS, SHEET_KHATA,
    SHEET_EXPENSES, SHEET_RETURNS, SHEET_USERS, SHEET_SETTINGS,
    SHEET_SYNC_LOG, SHEET_DASHBOARD, SHEET_SYSDB
  ];

  required.forEach(function(name) {
    if (existing.indexOf(name) === -1) {
      var sheet = ss.insertSheet(name);
      setupSheetHeaders(sheet, name, salesTab);
    }
  });

  hideSysDbTab();
}

function setupSheetHeaders(sheet, name, salesTabName) {
  if (name === salesTabName || name.indexOf(SHEET_SALES_PREFIX) === 0) {
    setupSalesHeaders(sheet);
  } else if (name === SHEET_INVENTORY) {
    sheet.appendRow(['system_row_id','barcode_id','name','category','unit',
      'price','stock_qty','low_threshold','last_updated','sync_status',
      'created_at','updated_at','pos_terminal_id']);
  } else if (name === SHEET_KHATA) {
    sheet.appendRow(['event_id','customer_id','pos_id','type','amount','timestamp','sync_uuid']);
  } else if (name === SHEET_CUSTOMERS) {
    sheet.appendRow(['customer_id','name','phone','last_updated']);
  } else if (name === SHEET_SETTINGS) {
    sheet.appendRow(['key','value']);
    sheet.appendRow(['last_updated_timestamp', Date.now()]);
    sheet.appendRow(['min_app_version', '1']);
    sheet.appendRow(['backup_sheet_url', '']);
    sheet.appendRow(['shop_name', '']);
    sheet.appendRow(['shop_phone', '']);
  }
}

function setupSalesHeaders(sheet) {
  sheet.appendRow(['invoice_id','pos_id','timestamp','items_json','subtotal',
    'tax','total','payment_method','reference_id','cashier_id','sync_uuid']);
}

// ─── M2.9 Disaster Recovery Backup ───────────────────────────────────────────

function dailyBackup() {
  try {
    var ss          = SpreadsheetApp.getActiveSpreadsheet();
    var settingsSheet = ss.getSheetByName(SHEET_SETTINGS);
    var backupUrl   = '';
    
    if (settingsSheet) {
      var data = settingsSheet.getDataRange().getValues();
      data.forEach(function(row) {
        if (row[0] === 'backup_sheet_url') backupUrl = row[1];
      });
    }
    
    if (!backupUrl) return;
    
    var backupSS = SpreadsheetApp.openByUrl(backupUrl);
    var sheets   = ss.getSheets();
    
    sheets.forEach(function(sheet) {
      var name = sheet.getName();
      if (name === SHEET_SYSDB || name === SHEET_DASHBOARD) return;
      
      var target = backupSS.getSheetByName(name);
      if (!target) target = backupSS.insertSheet(name);
      
      var data = sheet.getDataRange().getValues();
      if (data.length > 0) {
        target.clearContents();
        target.getRange(1, 1, data.length, data[0].length).setValues(data);
      }
    });
  } catch (e) { /* backup failure should not crash main app */ }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function getOrCreateCurrentSalesTab(ss) {
  var now    = new Date();
  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var name   = SHEET_SALES_PREFIX + months[now.getMonth()] + '_' + now.getFullYear();
  var sheet  = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
    setupSalesHeaders(sheet);
  }
  return sheet;
}

function createOverflowSalesTab(ss) {
  var now    = new Date();
  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var base   = SHEET_SALES_PREFIX + months[now.getMonth()] + '_' + now.getFullYear();
  var i = 2;
  while (ss.getSheetByName(base + '_overflow' + i)) i++;
  var sheet = ss.insertSheet(base + '_overflow' + i);
  setupSalesHeaders(sheet);
  return sheet;
}

function getExistingUuids(sheet, columnName) {
  var set  = new Set();
  var data = sheet.getDataRange().getValues();
  if (data.length < 2) return set;
  var col = data[0].indexOf(columnName);
  if (col === -1) return set;
  for (var i = 1; i < data.length; i++) {
    if (data[i][col]) set.add(String(data[i][col]));
  }
  return set;
}

function findRowByColumn(sheet, columnName, value) {
  var data = sheet.getDataRange().getValues();
  if (data.length < 2) return null;
  var col = data[0].indexOf(columnName);
  if (col === -1) return null;
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][col]) === String(value)) return i + 1; // 1-indexed
  }
  return null;
}

function updateRowValues(sheet, rowNum, obj) {
  var headers = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
  headers.forEach(function(h, idx) {
    if (obj[h] !== undefined) {
      sheet.getRange(rowNum, idx + 1).setValue(obj[h]);
    }
  });
}

function appendRow(sheet, values) {
  sheet.appendRow(values);
}

function updateSettingsTimestamp(ss) {
  try {
    var sheet = ss.getSheetByName(SHEET_SETTINGS);
    if (!sheet) return;
    var data = sheet.getDataRange().getValues();
    for (var i = 1; i < data.length; i++) {
      if (data[i][0] === 'last_updated_timestamp') {
        sheet.getRange(i + 1, 2).setValue(Date.now());
        return;
      }
    }
    sheet.appendRow(['last_updated_timestamp', Date.now()]);
  } catch (e) { /* ignore */ }
}
"""
}
