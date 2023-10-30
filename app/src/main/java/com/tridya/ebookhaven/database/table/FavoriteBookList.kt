package com.tridya.ebookhaven.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "FavoriteBookList")
data class FavoriteBookList(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo(name = "IsBookFavorite")
    var isBookFavorite: Boolean? = false,
    @ColumnInfo(name = "BookId")
    var bookId: Int? = 0,
    @ColumnInfo("authors")
    val authors: String,
    @ColumnInfo("languages")
    val languages: String,
    @ColumnInfo("subjects")
    val subjects: String,
    @ColumnInfo("image/jpeg")
    val imagejpeg: String?,
    @ColumnInfo("application/epub+zip")
    val applicationepubzip: String?,

    ) : Serializable



