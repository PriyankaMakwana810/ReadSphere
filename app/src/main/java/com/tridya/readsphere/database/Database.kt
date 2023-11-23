package com.tridya.readsphere.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tridya.readsphere.database.dao.BookListDao
import com.tridya.readsphere.database.dao.QuoteDao
import com.tridya.readsphere.database.table.BookListModel
import com.tridya.readsphere.database.table.Quote

@Database(
    entities = [BookListModel::class, Quote::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun bookDao(): BookListDao
    abstract fun quoteDao(): QuoteDao
}
