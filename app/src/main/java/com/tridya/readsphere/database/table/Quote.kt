package com.tridya.readsphere.database.table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_table")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val quoteText: String,
    val bookTitle: String,
    val pageNumber: Int,
    val webViewScrollY: Int
)
