package com.tridya.ebookhaven.database.dao

import androidx.room.*
import com.tridya.ebookhaven.database.table.BookListModel

@Dao
interface BookListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBook(userListModel: BookListModel)

    @Update
    fun updateBook(bookListModel: BookListModel)

    @Delete
    fun deleteBook(bookListModel: BookListModel)

    @Query("SELECT * FROM BookList")
    fun getAllBookList(): List<BookListModel>

    @Query("Select * from BookList WHERE BookTitle =:bookTitle")
    fun getBookData(bookTitle: String?): BookListModel

    @Query("SELECT * FROM BookList WHERE IsBookFavorite")
    fun getFavoriteBooks(): List<BookListModel>

    @Query("UPDATE BookList Set IsBookFavorite =:isBookFavorite WHERE BookTitle =:bookTitle ")
    fun updateIsBookFavorite(isBookFavorite: Boolean?, bookTitle: String?)

    @Query("SELECT IsBookFavorite FROM BookList WHERE BookTitle =:bookTitle")
    fun isBookFavorite(bookTitle: String?): Boolean

    @Query("SELECT IsBookLock FROM BookList WHERE BookTitle =:bookTitle")
    fun isBookLock(bookTitle: String?): Boolean

    @Query("SELECT BookPassword FROM BookList WHERE BookTitle =:bookTitle")
    fun bookPass(bookTitle: String?): Boolean

    @Query("UPDATE BookList Set IsBookLock =:isBookLock WHERE BookTitle =:bookTitle ")
    fun updateIsBookLock(isBookLock: Boolean?, bookTitle: String?)

    @Query("UPDATE BookList Set BookPassword =:bookPass WHERE BookTitle =:bookTitle ")
    fun updateBookPassword(bookPass: String?, bookTitle: String?)

    @Query("UPDATE BookList Set LastOpenedPage =:lastOpenedPage, LastOpenedPosition =:lastOpenedPosition WHERE BookTitle = :bookTitle ")
    fun updateLastOpenedPage(lastOpenedPage: Int?, lastOpenedPosition: Int?, bookTitle: String?)

    @Query("UPDATE BookList Set IsBookOpened =:isBookOpened WHERE BookTitle =:bookTitle ")
    fun updateIsBookOpened(isBookOpened: Boolean?, bookTitle: String?)

    @Query("SELECT IsBookOpened FROM BookList WHERE BookTitle =:bookTitle")
    fun isBookOpened(bookTitle: String?): Boolean

    @Query("SELECT LastOpenedPage FROM BookList WHERE BookTitle =:bookTitle")
    fun getLastOpenedPage(bookTitle: String?): Int

    @Query("SELECT LastOpenedPosition FROM BookList WHERE BookTitle =:bookTitle")
    fun getLastOpenedPosition(bookTitle: String?): Int
}
