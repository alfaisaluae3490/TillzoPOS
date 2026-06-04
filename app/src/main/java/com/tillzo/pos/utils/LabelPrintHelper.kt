package com.tillzo.pos.utils

import javax.inject.Inject

enum class LabelSize {
    SMALL, MEDIUM
}

class LabelPrintHelper @Inject constructor() {

    fun printLabel(barcodeId: String, productName: String, price: Double, qty: Int, labelSize: LabelSize): String {
        // Construct TSPL/ZPL Commands based on size
        return when (labelSize) {
            LabelSize.SMALL -> { // 0.5" x 1"
                """
                SIZE 25.4 mm, 12.7 mm
                GAP 3 mm, 0 mm
                SPEED 4
                DENSITY 8
                DIRECTION 0
                REFERENCE 0,0
                CLS
                TEXT 10,10,"2",0,1,1,"$productName"
                TEXT 10,40,"2",0,1,1,"Rs $price"
                QRCODE 150,10,L,2,A,0,"$barcodeId"
                PRINT $qty
                """.trimIndent()
            }
            LabelSize.MEDIUM -> { // 1" x 1.5"
                """
                SIZE 38.1 mm, 25.4 mm
                GAP 3 mm, 0 mm
                SPEED 4
                DENSITY 8
                DIRECTION 0
                REFERENCE 0,0
                CLS
                TEXT 20,20,"3",0,1,1,"$productName"
                TEXT 20,60,"3",0,1,1,"Price: Rs $price"
                TEXT 20,100,"2",0,1,1,"ID: $barcodeId"
                QRCODE 200,20,L,4,A,0,"$barcodeId"
                PRINT $qty
                """.trimIndent()
            }
        }
    }
}
