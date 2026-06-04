package com.tillzo.pos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tillzo.pos.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Smoke test — AppDatabase.
 * Verifies Room can initialize the database in-memory without errors.
 *
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Test only
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun database_createsSuccessfully() {
        assertNotNull("AppDatabase should not be null after initialization", db)
    }

    @Test
    fun database_isOpen() {
        assert(db.isOpen) { "AppDatabase should be open after creation" }
    }
}
