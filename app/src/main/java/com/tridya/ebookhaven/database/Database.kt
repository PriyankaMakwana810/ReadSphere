package com.tridya.ebookhaven.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tridya.ebookhaven.database.dao.BookListDao
import com.tridya.ebookhaven.database.table.BookListModel

@Database(
    entities = [BookListModel::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun bookDao(): BookListDao
}
