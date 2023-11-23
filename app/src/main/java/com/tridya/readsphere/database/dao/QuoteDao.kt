package com.tridya.readsphere.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tridya.readsphere.database.table.Quote

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quote_table WHERE bookTitle = :bookTitle")
    fun getAllQuotes(bookTitle: String): LiveData<List<Quote>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuote(quote: Quote)

    @Delete
    fun deleteQuote(quote: Quote)

    @Query("DELETE FROM quote_table")
    fun deleteAllQuotes()


    @Query("DELETE FROM quote_table  WHERE quoteText = :quoteText AND bookTitle = :bookTitle AND pageNumber =:pageNumber")
    fun deleteSpecificQuote(quoteText: String, bookTitle: String, pageNumber: Int?)

    @Query("SELECT * FROM quote_table WHERE quoteText = :quoteText AND pageNumber =:pageNumber")
    fun isQuoteIsExisting(quoteText: String, pageNumber: Int?): Quote
}
