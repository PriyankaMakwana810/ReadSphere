package com.tridya.readsphere.activities

import android.os.Bundle
import android.view.View
import androidx.room.Room
import com.tridya.readsphere.R
import com.tridya.readsphere.base.BaseActivity
import com.tridya.readsphere.database.Database
import com.tridya.readsphere.database.dao.BookListDao
import com.tridya.readsphere.databinding.ActivityBookInfoBinding
import com.tridya.readsphere.utils.FindTitle
import com.tridya.readsphere.utils.GlideUtils

class BookInfoActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityBookInfoBinding
    private var findTitle = FindTitle()

    var isFavorite = false
    private var bookTitle: String? = null
    lateinit var bookListDao: BookListDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val ROOM_DB = getString(R.string.book_database)
        val db = Room.databaseBuilder(
            applicationContext, Database::class.java, ROOM_DB
        ).allowMainThreadQueries().build()
        bookListDao = db.bookDao()
        binding.ivBack.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)
        binding.ivFavorites.setOnClickListener(this)

        bookTitle = intent.getStringExtra("title")

        isFavorite = bookListDao.isBookFavorite(bookTitle)

        val bookInfo = bookListDao.getBookData(bookTitle)

        binding.tvTitle.text = bookTitle
        binding.tvBookName.text = bookInfo.bookTitle
        binding.tvAuthorName.text = bookInfo.bookAuthor
        binding.tvBookPages.text = getString(R.string.pages, bookInfo.totalPages.toString())
        binding.tvPublisherName.text = getString(R.string.publisher_, bookInfo.bookPublisher)
        try {
            binding.tvPreview.text = findTitle.FindDescription(bookInfo.bookPath)
        } catch (e: Exception) {
            binding.tvPreview.text = " "
        }

        isFavorite = bookListDao.isBookFavorite(bookTitle)
        binding.ivFavorites.isSelected = isFavorite

        GlideUtils(this).cornerRadius(16)
            .loadImageLibrary(bookInfo.imagejpeg, binding.imgBookInfo)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivBack.id -> {
                this.finish()
            }

            binding.ivFavorites.id -> {
                if (isFavorite) {
                    binding.ivFavorites.isSelected = false
                    bookListDao.updateIsBookFavorite(false, bookTitle)
                } else {
                    binding.ivFavorites.isSelected = true
                    bookListDao.updateIsBookFavorite(true, bookTitle)
                }
            }
        }
    }

}
