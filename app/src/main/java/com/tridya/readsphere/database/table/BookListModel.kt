package com.tridya.readsphere.database.table

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
    @ColumnInfo("BookAuthor")
    var bookAuthor: String? = null,
    @ColumnInfo("BookPath")
    var bookPath: String? = null,
    @ColumnInfo("BookLanguage")
    var bookLanguage: String? = null,
    @ColumnInfo("Publisher")
    var bookPublisher: String? = null,
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
    @ColumnInfo(name = "IsBookFavorite")
    var isBookFavorite: Boolean? = false,
    @ColumnInfo("CoverImage")
    val imagejpeg: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
    ) : Serializable


