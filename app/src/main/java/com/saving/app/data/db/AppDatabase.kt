package com.saving.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val DEFAULT_CATEGORIES = listOf("Food", "Recharge", "Medical", "Home", "Petrol")

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            lateinit var instance: AppDatabase
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "saving.db"
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        DEFAULT_CATEGORIES.forEach { name ->
                            instance.categoryDao().insert(CategoryEntity(name = name))
                        }
                    }
                }
            }).build()
            return instance
        }
    }
}
