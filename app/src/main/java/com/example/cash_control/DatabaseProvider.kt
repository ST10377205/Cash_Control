package com.example.cash_control
import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "my_db"
            )
            .fallbackToDestructiveMigration() // ✅ This prevents the crash by rebuilding the DB on version changes
            .build()
        }
        return db!!
    }
}