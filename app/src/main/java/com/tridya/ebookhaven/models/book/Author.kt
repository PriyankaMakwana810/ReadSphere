package com.tridya.ebookhaven.models.book

import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("name")
    val name: String = "N/A",
    @SerializedName("birth_year")
    val birthYear: Int,
    @SerializedName("death_year")
    val deathYear: Int
)