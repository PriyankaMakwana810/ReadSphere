package com.tridya.ebookhaven.models.book

import java.io.File

data class BookInfo(
    val bookTitle: String,
    val bookAuthor: String,
    val bookCover: File,
    val bookPath: String,
    val importTime: String,
    val openTime: Int,
    val currentPage: Int,
    val currentScroll: Int,
) : java.io.Serializable