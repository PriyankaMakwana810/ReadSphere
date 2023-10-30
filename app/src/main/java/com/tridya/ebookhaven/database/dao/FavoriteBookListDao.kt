package com.tridya.ebookhaven.database.dao

import androidx.room.*
import com.tridya.ebookhaven.database.table.FavoriteBookList

@Dao
interface FavoriteBookListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFavoriteBook(favoriteBookList: FavoriteBookList)

    @Update
    fun updateFavoriteBook(favoriteBookList: FavoriteBookList)

    @Delete
    fun deleteFavoriteBook(favoriteBookList: FavoriteBookList)

    @Query("SELECT * FROM FavoriteBookList WHERE IsBookFavorite ORDER BY id DESC")
    fun gerAllFavoriteBooks(): List<FavoriteBookList>

    @Query("Select * from FavoriteBookList WHERE title =:bookTitle")
    fun getFavoriteBooks(bookTitle: String?): FavoriteBookList

    @Query("UPDATE FavoriteBookList Set isBookFavorite =:isFavorite WHERE title = :bookTitle ")
    fun updateIsBookFavorite(isFavorite: Boolean?, bookTitle: String?)

    @Query("SELECT isBookFavorite FROM FavoriteBookList WHERE title = :bookTitle")
    fun isBookFavorite(bookTitle: String?): Boolean
}
