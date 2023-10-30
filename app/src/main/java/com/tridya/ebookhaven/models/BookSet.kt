package com.tridya.ebookhaven.models

import com.google.gson.annotations.SerializedName
import com.tridya.ebookhaven.models.book.Book

data class BookSet(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    val books: List<Book>
)