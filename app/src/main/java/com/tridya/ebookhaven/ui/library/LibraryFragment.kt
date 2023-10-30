package com.tridya.ebookhaven.ui.library

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.base.BaseFragment
import com.tridya.ebookhaven.database.table.BookListModel
import com.tridya.ebookhaven.databinding.FragmentLibraryBinding
import com.tridya.ebookhaven.ui.home.adapter.LibraryAdapter
import com.tridya.ebookhaven.utils.gone
import com.tridya.ebookhaven.utils.visible
import java.io.File
import java.util.Arrays
import java.util.Objects

class LibraryFragment : BaseFragment(), LibraryAdapter.onItemClick {
    private lateinit var binding: FragmentLibraryBinding
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var bookList: ArrayList<BookListModel>
    var files: Array<File>? = null
    val DOWNLOAD_DIR = "eBookHavenEbooks"
    lateinit var downloadedAdapter: LibraryAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                activity?.onBackPressed()
                findNavController().navigate(R.id.fragmentHome)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
//        binding.toolBar.searchView.gone()
        binding.toolBar.tvTitle.text = getString(R.string.library)
        binding.toolBar.ivFavorites.gone()
        binding.toolBar.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.layoutManager = layoutManager
        bookList = bookListDao.getAllBookList() as ArrayList<BookListModel>
        if (bookList.isEmpty()) {
            binding.rvBooks.gone()
            binding.tvNothing.visible()
        } else {
            downloadedAdapter = LibraryAdapter(bookList, this)
            binding.rvBooks.adapter = downloadedAdapter
        }
    }

    override fun onBookSelected(book: BookListModel) {
        val file = File(book.bookPath.toString())
        if (file.exists()) {
            /* val intentEpubViewer = Intent(activity, EpubViewer::class.java)
 //                    intentEpubViewer.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
             intentEpubViewer.putExtra("title", book.bookTitle)
             intentEpubViewer.putExtra("path", book.bookPath)
             intentEpubViewer.putExtra("author", book.bookAuthor)
             intentEpubViewer.putExtra("bookCoverImage", book.imagejpeg)
             intentEpubViewer.putExtra("currentPage", book.lastOpenedPage)
             intentEpubViewer.putExtra("currentScroll", book.lastOpenedPosition)
             intentEpubViewer.putExtra("BookId", book.bookId)
             this.startActivity(intentEpubViewer)*/

        } else {
            /* MessageDialog(requireContext()).setTitle(getString(R.string.file_not_found))
                 .setMessage(getString(R.string.book_may_be_deleted_from_internal_storage_please_re_download_book))
                 .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                     dialog.dismiss()
                 }.show()*/
            bookListDao.deleteBook(book)
            downloadedAdapter.notifyItemRemoved(bookList.indexOf(book))
            bookList.remove(book)
            if (bookList.isEmpty()) {
                binding.tvNothing.visible()
            }
        }
    }

    private fun setData() {
        val path =
            Objects.requireNonNull(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).absolutePath + File.separator + DOWNLOAD_DIR
        Log.e("Files", "Path: $path")
        val directory = File(path)
        files = directory.listFiles()
        if (files != null) {
            Arrays.sort(files!!) { o1, o2 ->
                if ((o1 as File).lastModified() > (o2 as File).lastModified()) {
                    -1
                } else if (o1.lastModified() < o2.lastModified()) {
                    +1
                } else {
                    0
                }
            }
        }
        setDataToRecyclerView()
    }

    private fun setDataToRecyclerView() {
        if (files != null && files!!.isNotEmpty()) {
            binding.rvBooks.visibility = View.VISIBLE
            binding.tvNothing.visibility = View.GONE
            binding.rvBooks.layoutManager = GridLayoutManager(mActivity, 3)
        } else {
            binding.rvBooks.visibility = View.GONE
            binding.tvNothing.visibility = View.VISIBLE
        }
    }
}