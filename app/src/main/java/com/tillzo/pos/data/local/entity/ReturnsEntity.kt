package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * GAP-3 FIX (2026-08-23): Returns table was vestigial — the "Returns" sheet
 * tab existed with headers but was NEVER populated because there was no
 * ReturnsEntity/DAO at all. Returns were only visible as negative-sale rows
 * in Sales. Now every processed return (Restock or Damaged/Wastage) also
 * writes one ReturnsEntity row per returned item → synced to the Returns tab.
 *
 * Column order matches the Returns sheet tab headers:
 * return_id, system_row_id, original_invoice_id, item_id, qty_returned,
 * condition, refund_method, amount, last_updated
 */
@Entity(tableName = "returns_log")
data class ReturnsEntity(
    @PrimaryKey
    val returnId: String = UUID.randomUUID().toString(),
    val systemRowId: String,              // unique sync id (same as returnId)
    val originalInvoiceId: String,        // original sale invoice_id
    val itemId: String,                   // product system_row_id
    val qtyReturned: Double,
    val condition: String,                // RESTOCK | DAMAGED | WASTAGE
    val refundMethod: String,             // CASH | CARD | WALLET | UDHAAR
    val amount: Double,                   // refunded amount for this item
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: String = "pending",
    val posTerminalId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

fun ReturnsEntity.toSheetRow(): List<Any> = listOf(
    returnId, systemRowId, originalInvoiceId, itemId, qtyReturned,
    condition, refundMethod, amount, lastUpdated, syncStatus,
    // FIX (2026-08-23, RUN #11): positional append — value order ko sheet
    // header order se match karna zaroori hai (13 cols). Pehle sirf 12 values
    // the: posTerminalId created_at column mein, createdAt updated_at mein
    // likha jata tha, pos_terminal_id hamesha empty rehta tha.
    createdAt, lastUpdated, posTerminalId
)
