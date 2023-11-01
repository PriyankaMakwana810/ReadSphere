package com.tridya.ebookhaven.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tridya.ebookhaven.BuildConfig
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.activities.EpubViewer
import com.tridya.ebookhaven.base.BaseFragment
import com.tridya.ebookhaven.database.table.BookListModel
import com.tridya.ebookhaven.databinding.FragmentHomeBinding
import com.tridya.ebookhaven.dialogs.MessageDialog
import com.tridya.ebookhaven.ui.home.adapter.LibraryAdapter
import com.tridya.ebookhaven.utils.gone
import com.tridya.ebookhaven.utils.visible
import java.io.File

class HomeFragment : BaseFragment(), LibraryAdapter.onItemClick {
    private lateinit var binding: FragmentHomeBinding
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var bookList: ArrayList<BookListModel>
    var files: Array<File>? = null
    lateinit var downloadedAdapter: LibraryAdapter
    var isPermissionGranted = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        binding.toolBar.tvTitle.text = getString(R.string.recent_books)
        binding.toolBar.ivFavorites.gone()
        binding.toolBar.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        checkPermissions()

        if (isPermissionGranted) {
            layoutManager = LinearLayoutManager(requireContext())
            binding.rvBooks.layoutManager = layoutManager
            bookList = bookListDao.getAllBookList() as ArrayList<BookListModel>
            if (bookList.isEmpty()) {
                binding.rvBooks.gone()
                binding.llNothingError.visible()
            } else {
                downloadedAdapter = LibraryAdapter(bookList, this)
                binding.rvBooks.adapter = downloadedAdapter
            }
        } else {
            MessageDialog(mContext).setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.storage_perm_error))
                .setPositiveButton(getString(R.string.ok))
                { dialog, _ ->
                    dialog.dismiss()
                    findNavController().navigate(
                        R.id.fragmentHome,
                        arguments,
                        NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build()
                    )
                }.cancelable(false).show()
        }

    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                startActivity(intent)
            } else {
                isPermissionGranted = true
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            } else {
                isPermissionGranted = true
            }
        }
    }

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                return@registerForActivityResult
            } else {
                showToastShort("Please Grant necessary permissions!!")
            }
        }

    override fun onBookSelected(book: BookListModel) {
        val file = File(book.bookPath.toString())
        if (file.exists()) {
            val intentEpubViewer = Intent(activity, EpubViewer::class.java)
            intentEpubViewer.putExtra("title", book.bookTitle)
            intentEpubViewer.putExtra("path", book.bookPath)
            intentEpubViewer.putExtra("author", book.bookAuthor)
            intentEpubViewer.putExtra("bookLanguage", book.bookLanguage)
            intentEpubViewer.putExtra("publisher", book.bookPublisher)
            intentEpubViewer.putExtra("bookCoverImage", book.imagejpeg.toString())
            intentEpubViewer.putExtra("currentPage", book.lastOpenedPage)
            intentEpubViewer.putExtra("currentScroll", book.lastOpenedPosition)
            this.startActivity(intentEpubViewer)
        } else {
            bookListDao.deleteBook(book)
            downloadedAdapter.notifyItemRemoved(bookList.indexOf(book))
            bookList.remove(book)
            if (bookList.isEmpty()) {
                binding.llNothingError.visible()
            }
        }
    }

}