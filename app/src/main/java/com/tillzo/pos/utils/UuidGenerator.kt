package com.tillzo.pos.utils

import java.util.UUID

/**
 * UUID generator utility.
 * Every entity gets a unique, immutable system_row_id at creation time.
 * This is the canonical PK — never overwritten, never reused.
 */
object UuidGenerator {
    fun generate(): String = UUID.randomUUID().toString()
}
