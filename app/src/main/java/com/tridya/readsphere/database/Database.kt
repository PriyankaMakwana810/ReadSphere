package com.tridya.readsphere.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tridya.readsphere.database.dao.BookListDao
import com.tridya.readsphere.database.table.BookListModel

@Database(
    entities = [BookListModel::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun bookDao(): BookListDao
}
