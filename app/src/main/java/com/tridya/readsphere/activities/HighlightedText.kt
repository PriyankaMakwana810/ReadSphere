package com.tridya.readsphere.activities

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.room.Room
import com.tridya.readsphere.R
import com.tridya.readsphere.database.Database
import com.tridya.readsphere.database.table.Quote
import com.tridya.readsphere.utils.CustomWebView
import java.util.Objects

class HighlightedText(private val context: Context, private val webView: CustomWebView) {
    private val ROOM_DB = context.getString(R.string.book_database)
    private val db = Room.databaseBuilder(
        context, Database::class.java, ROOM_DB
    ).allowMainThreadQueries().build()
    private var quoteDao = db.quoteDao()
    private val quoteList: MutableList<Quote> = mutableListOf()

    fun getQuotes(bookTitle: String): MutableList<Quote> {
        quoteDao.getAllQuotes().observeForever(Observer { quotes ->
            quoteList.clear()
            quoteList.addAll(quotes)
        })
        return quoteList
    }

    fun highlightQuote(pageNumber: Int) {
        // Highlight quotes in WebView
        webView.evaluateJavascript(
            "function doSearch(text, backgroundColor) {\n" +
                    "    if (window.find && window.getSelection) {\n" +
                    "        var windowHeight = window.scrollY;\n" +
                    "        document.designMode = 'on';\n" +
                    "        var sel = window.getSelection();\n" +
                    "        sel.collapse(document.body, 0);\n" +
                    "        while (window.find(text)) {\n" +
                    "            document.execCommand('HiliteColor', false, backgroundColor);\n" +
                    "            sel.collapseToEnd();\n" +
                    "        }\n" +
                    "        document.designMode = 'off';\n" +
                    "        window.scrollTo(0, windowHeight);\n" +
                    "    }\n" +
                    "}", null
        )

        for (i in quoteList.indices) {
            if ((pageNumber == quoteList[i].pageNumber)) {
                val editedQuote: String =
                    quoteList[i].quoteText.replace("'".toRegex(), "\\\\'")
                if (editedQuote.contains(Objects.requireNonNull(System.getProperty("line.separator")))) {
                    val editedQuotes = editedQuote.split(
                        Objects.requireNonNull(System.getProperty("line.separator")).toRegex()
                    ).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    for (quote: String in editedQuotes) {
                        webView.evaluateJavascript("doSearch('$quote', 'Yellow')", null)
                    }
                } else {
                    webView.evaluateJavascript("doSearch('$editedQuote', 'Yellow')", null)
                }
            }
        }
    }

    fun removeQuote(quoteText: String, bookTitle: String, pageNumber: Int, themeBack: String) {
//        quoteDao.deleteQuote(quote)
        val existingQuote = quoteDao.isQuoteIsExisting(quoteText, pageNumber)
        quoteDao.deleteSpecificQuote(quoteText, bookTitle, pageNumber)
        quoteList.remove(existingQuote)
        // Process removal...
    }

    fun addQuote(quoteText: String, bookTitle: String, pageNumber: Int, webViewScrollY: Int) {
        if (!isQuoteExistOnPage(quoteText, pageNumber)) {
            val newQuote = Quote(
                quoteText = quoteText,
                bookTitle = bookTitle,
                pageNumber = pageNumber,
                webViewScrollY = webViewScrollY
            )
            quoteDao.insertQuote(newQuote)
        } else {
            Toast.makeText(context, "quote already exist!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isQuoteExistOnPage(quoteText: String, pageNumber: Int): Boolean {
        val existingQuote = quoteDao.isQuoteIsExisting(quoteText, pageNumber)
        return existingQuote != null
    }

}