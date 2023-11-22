package com.tridya.readsphere.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tridya.readsphere.R
import com.tridya.readsphere.activities.EpubViewer
import com.tridya.readsphere.base.BaseFragment
import com.tridya.readsphere.database.table.BookListModel
import com.tridya.readsphere.databinding.FragmentFavoritesBinding
import com.tridya.readsphere.ui.home.adapter.FavoritesAdapter
import com.tridya.readsphere.utils.gone
import com.tridya.readsphere.utils.visible

class FavoritesFragment : BaseFragment(),
    FavoritesAdapter.onFavoriteItemClick {
    private lateinit var binding: FragmentFavoritesBinding
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var favoriteBookList: ArrayList<BookListModel>
    private var adapterFavorites: FavoritesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFavoritesBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolBar.ivBack.visible()
        binding.toolBar.ivFavorites.gone()

        binding.toolBar.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.layoutManager = layoutManager

        binding.toolBar.tvTitle.text = getString(R.string.favorites)
        favoriteBookList = bookListDao.getFavoriteBooks() as ArrayList<BookListModel>
        if (favoriteBookList.isEmpty()) {
            binding.rvBooks.gone()
            binding.tvNothing.visible()
        } else {
            adapterFavorites =
                FavoritesAdapter(favoriteBookList, this)
            binding.rvBooks.adapter = adapterFavorites
        }
    }

    override fun onFavoriteBookSelected(bookData: BookListModel) {
        val intentEpubViewer = Intent(activity, EpubViewer::class.java)
        intentEpubViewer.putExtra("title", bookData.bookTitle)
        intentEpubViewer.putExtra("path", bookData.bookPath)
        intentEpubViewer.putExtra("author", bookData.bookAuthor)
        intentEpubViewer.putExtra("bookLanguage", bookData.bookLanguage)
        intentEpubViewer.putExtra("publisher", bookData.bookPublisher)
        intentEpubViewer.putExtra("bookCoverImage", bookData.imagejpeg.toString())
        intentEpubViewer.putExtra("currentPage", bookData.lastOpenedPage)
        intentEpubViewer.putExtra("currentScroll", bookData.lastOpenedPosition)
        this.startActivity(intentEpubViewer)
    }
}