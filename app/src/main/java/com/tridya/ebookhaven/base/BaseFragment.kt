package com.tridya.ebookhaven.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.database.Database
import com.tridya.ebookhaven.database.dao.BookListDao
import com.tridya.ebookhaven.database.dao.FavoriteBookListDao

open class BaseFragment : Fragment() {
    lateinit var mContext: Context
    lateinit var mActivity: Activity
    lateinit var bookListDao: BookListDao
    lateinit var favoriteBookListDao: FavoriteBookListDao

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        mActivity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ROOM_DB = getString(R.string.book_database)
        val db = Room.databaseBuilder(
            mContext, Database::class.java, ROOM_DB
        ).allowMainThreadQueries().build()
        bookListDao = db.bookDao()
        favoriteBookListDao = db.favoriteBooksDao()

    }

    fun showToastShort(message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    fun showToastLong(message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
    }
    @JvmOverloads
    fun setUpToolbarWithBackArrow(strTitle: String? = null, isBackArrow: Boolean = true) {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        val title = toolbar?.findViewById<TextView>(R.id.tvTitle)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(isBackArrow)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
            if (strTitle != null) title?.text = strTitle
        }
    }

}
