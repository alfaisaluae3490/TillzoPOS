package com.tillzo.pos.domain.usecase.grn

import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.domain.repository.ConfirmGrnResult
import com.tillzo.pos.domain.repository.GrnRepository
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.domain.repository.ProductBatchRepository
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ConfirmGrnUseCase @Inject constructor(
    private val grnRepository: GrnRepository,
    private val inventoryRepository: InventoryRepository,
    private val productBatchRepository: ProductBatchRepository,
    private val purchaseOrderDao: PurchaseOrderDao
) {
    suspend operator fun invoke(grnId: String): ConfirmGrnResult {
        return withContext(Dispatchers.IO) {
            try {
                val grnHeader = grnRepository.getGrnById(grnId)
                    ?: return@withContext ConfirmGrnResult(false, 0, 0, 0, "GRN not found")

                // FIX (2026-08-22, DEF-40): non-idempotent confirm — double-tap
                // on "Confirm GRN" (or a retry after a timeout) re-applied the
                // batch stock, DOUBLE-incrementing inventory and creating
                // duplicate batches. A CONFIRMED GRN is now a terminal state:
                // re-invoke returns success without re-applying anything.
                if (grnHeader.status.equals("CONFIRMED", ignoreCase = true)) {
                    return@withContext ConfirmGrnResult(true, 0, 0, 0, "Already confirmed (idempotent)")
                }

                val grnItems = grnRepository.getGrnItems(grnId)
                var newProductsCreated = 0
                var batchesAdded = 0
                var batchesUpdated = 0

                val posTerminalId = grnHeader.posTerminalId.takeIf { it.isNotBlank() } ?: "terminal_1"

                for (item in grnItems) {
                    when (item.inventoryAction) {

                        "NEW_PRODUCT", "NEW_ITEM" -> {
                            // Step 1: Create new ProductEntity (InventoryEntity in Tillzo)
                            val newProduct = InventoryEntity(
                                item_name = item.productName,
                                category = item.categoryId.takeIf { it.isNotBlank() } ?: "Uncategorized",
                                barcode_id = item.barcodeId.takeIf { it.isNotBlank() } ?: item.productId,
                                unit = item.unit,
                                price_per_unit = item.sellingPrice,
                                current_stock = item.receivedQty,
                                low_stock_threshold = item.lowStockThreshold,
                                sku = item.sku,
                                brand = item.brand,
                                description = "",
                                cost_price = item.unitCostPrice,
                                tax_percent = 0.0,
                                batch_number = effectiveBatchNumber(item, grnHeader.grnNumber),
                                expiry_date = item.expiryDate,
                                manufacturing_date = item.manufacturingDate,
                                is_damaged_stock = false,
                                damaged_qty = 0.0,
                                totalStock = item.receivedQty,
                                hasBatches = true,
                                pos_terminal_id = posTerminalId
                            )
                            inventoryRepository.insertItem(newProduct)

                            // Step 2: Create first batch for this new product
                            val batchId = UUID.randomUUID().toString()
                            val newBatch = ProductBatchEntity(
                                batchId = batchId,
                                productId = newProduct.system_row_id,
                                barcodeId = newProduct.barcode_id,
                                batchNumber = effectiveBatchNumber(item, grnHeader.grnNumber),
                                manufacturingDate = item.manufacturingDate,
                                expiryDate = item.expiryDate,
                                stockQty = item.receivedQty,
                                costPrice = item.unitCostPrice,
                                sellingPrice = item.sellingPrice,
                                isActive = true,
                                posTerminalId = posTerminalId
                            )
                            productBatchRepository.insertBatch(newBatch)

                            // Step 3: Update grnItem with new batchId
                            grnRepository.updateGrnItemBatchId(item.grnItemId, batchId)
                            newProductsCreated++
                        }

                        "ADD_BATCH" -> {
                            // Item exists. Add new batch to existing product.
                            val batchId = UUID.randomUUID().toString()
                            val newBatch = ProductBatchEntity(
                                batchId = batchId,
                                productId = item.productId,
                                barcodeId = item.barcodeId.takeIf { it.isNotBlank() } ?: item.productId,
                                batchNumber = effectiveBatchNumber(item, grnHeader.grnNumber),
                                manufacturingDate = item.manufacturingDate,
                                expiryDate = item.expiryDate,
                                stockQty = item.receivedQty,
                                costPrice = item.unitCostPrice,
                                sellingPrice = item.sellingPrice,
                                isActive = true,
                                posTerminalId = posTerminalId
                            )
                            productBatchRepository.insertBatch(newBatch)

                            // Recalculate totalStock = sum of all active batches
                            inventoryRepository.recalculateTotalStock(item.productId)

                            // Update barcodeId on grnItem (and batchId)
                            grnRepository.updateGrnItemBatchId(item.grnItemId, batchId)
                            batchesAdded++
                        }

                        "UPDATE_BATCH", "PENDING" -> {
                            // Item exists. Look up batch by batchNumber, then fall back to batchId.
                            val existingBatch = item.batchNumber.takeIf { it.isNotBlank() }
                                ?.let { productBatchRepository.getBatchByNumber(item.productId, it) }
                            val targetBatchId = existingBatch?.batchId
                                ?: item.batchId.takeIf { it.isNotBlank() }

                            if (targetBatchId != null) {
                                productBatchRepository.incrementBatchStock(
                                    batchId = targetBatchId,
                                    additionalQty = item.receivedQty
                                )
                                batchesUpdated++
                            } else {
                                // Fallback: create new batch under this product
                                val newBatchId = UUID.randomUUID().toString()
                                val newBatch = ProductBatchEntity(
                                    batchId = newBatchId,
                                    productId = item.productId,
                                    barcodeId = item.barcodeId.takeIf { it.isNotBlank() } ?: item.productId,
                                    batchNumber = effectiveBatchNumber(item, grnHeader.grnNumber),
                                    manufacturingDate = item.manufacturingDate,
                                    expiryDate = item.expiryDate,
                                    stockQty = item.receivedQty,
                                    costPrice = item.unitCostPrice,
                                    sellingPrice = item.sellingPrice,
                                    isActive = true,
                                    posTerminalId = posTerminalId
                                )
                                productBatchRepository.insertBatch(newBatch)
                                grnRepository.updateGrnItemBatchId(item.grnItemId, newBatchId)
                                batchesAdded++
                            }
                            // Recalculate totalStock
                            inventoryRepository.recalculateTotalStock(item.productId)
                        }
                    }
                }

                // Mark GRN as CONFIRMED
                grnRepository.updateGrnStatus(grnId, "CONFIRMED")

                // Update linked PO status
                if (grnHeader.poId.isNotEmpty()) {
                    updateLinkedPOStatus(grnHeader.poId, grnItems)
                }

                ConfirmGrnResult(
                    success = true,
                    newProductsCreated = newProductsCreated,
                    batchesAdded = batchesAdded,
                    batchesUpdated = batchesUpdated
                )

            } catch (e: Exception) {
                ConfirmGrnResult(false, 0, 0, 0, e.message)
            }
        }
    }

    private suspend fun updateLinkedPOStatus(poId: String, grnItems: List<com.tillzo.pos.data.local.entity.GrnItemEntity> = emptyList()) {
        // FIX (2026-08-22, DEF-44): increment each PO item's receivedQty by the
        // quantity received in THIS GRN — previously receivedQty was never
        // touched, so allFullyReceived/anyReceived stayed false and the PO was
        // stuck at "SENT" forever (DEF-10 root cause). Only for planned GRNs
        // (poItemId non-empty).
        if (poId.isNotBlank()) {
            val now = System.currentTimeMillis()
            grnItems.forEach { grnItem ->
                if (grnItem.poItemId.isNotBlank() && grnItem.receivedQty > 0) {
                    try {
                        purchaseOrderDao.incrementReceivedQty(grnItem.poItemId, grnItem.receivedQty, now)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to increment receivedQty for ${grnItem.poItemId}: ${e.message}")
                    }
                }
            }
        }
        val poItems = purchaseOrderDao.getPOItems(poId)
        val allFullyReceived = poItems.isNotEmpty() && poItems.all { it.receivedQty >= it.orderedQty }
        val anyReceived = poItems.any { it.receivedQty > 0 }
        val newStatus = when {
            allFullyReceived -> "RECEIVED"
            anyReceived -> "PARTIALLY_RECEIVED"
            else -> "SENT" // Or whatever default
        }
        purchaseOrderDao.updatePOStatus(poId, newStatus, System.currentTimeMillis())
    }

    companion object {
        private const val TAG = "ConfirmGrnUseCase"
    }

    /**
     * FIX (2026-08-26): blank batch number (manual GRN entry chhod deta tha)
     * → auto-generate so every batch is trackable (GRN detail "PENDING"
     * badge, FIFO deduction, sheet sync sabko number chahiye).
     */
    private fun effectiveBatchNumber(
        item: com.tillzo.pos.data.local.entity.GrnItemEntity,
        grnNumber: String
    ): String {
        if (item.batchNumber.isNotBlank()) return item.batchNumber
        val seed = listOf(item.sku, item.barcodeId, item.productId)
            .firstOrNull { it.isNotBlank() } ?: "NEW"
        return "B-${grnNumber.takeLast(4)}-${seed.take(4).uppercase()}"
    }
}
