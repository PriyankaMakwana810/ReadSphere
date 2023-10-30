package com.tridya.ebookhaven.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "BookList")
data class BookListModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo("BookTitle")
    var bookTitle: String? = null,
    @ColumnInfo("Author")
    var bookAuthor: String? = null,
    @ColumnInfo("BookPath")
    var bookPath: String? = null,
    @ColumnInfo(name = "TotalPages")
    var totalPages: Int? = 0,
    @ColumnInfo(name = "IsBookLock")
    var isBookLock: Boolean? = false,
    @ColumnInfo("BookPassword")
    var bookPassword: String? = null,
    @ColumnInfo(name = "IsBookOpened")
    var isBookOpened: Boolean? = false,
    @ColumnInfo(name = "LastOpenedPage")
    var lastOpenedPage: Int? = 0,
    @ColumnInfo(name = "LastOpenedPosition")
    var lastOpenedPosition: Int? = 0,
    @ColumnInfo(name = "BookId")
    var bookId: Int? = 0,
    @ColumnInfo("CoverImage")
    val imagejpeg: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
    ) : Serializable


