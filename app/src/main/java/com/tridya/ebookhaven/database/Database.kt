package com.tridya.ebookhaven.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tridya.ebookhaven.database.dao.BookListDao
import com.tridya.ebookhaven.database.dao.FavoriteBookListDao
import com.tridya.ebookhaven.database.table.BookListModel
import com.tridya.ebookhaven.database.table.FavoriteBookList

@Database(
    entities = [BookListModel::class, FavoriteBookList::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun bookDao(): BookListDao
    abstract fun favoriteBooksDao(): FavoriteBookListDao
}
