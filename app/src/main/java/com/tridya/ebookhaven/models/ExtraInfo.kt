package com.tridya.ebookhaven.models

/** Extra info from google books API */
data class ExtraInfo(
    val coverImage: String = "",
    val pageCount: Int = 0,
    val description: String = "",
    val averageRating: String = "",
    val title: String = "",
    val publisher: String = "",
    val publishedDate: String = "",
)
