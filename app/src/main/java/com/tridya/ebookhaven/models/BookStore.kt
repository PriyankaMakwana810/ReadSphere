package com.tridya.ebookhaven.models

import com.tridya.ebookhaven.models.book.Book

object BookStore {
    private var book: Book? = null

    fun setBook(book: Book) {
        BookStore.book = book
    }

    fun getBook(): Book? {
        return book
    }
}
