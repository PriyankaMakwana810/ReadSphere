package com.tridya.ebookhaven.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tridya.ebookhaven.BuildConfig
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.base.BaseFragment
import com.tridya.ebookhaven.databinding.FragmentHomeBinding
import com.tridya.ebookhaven.models.book.BookInfo
import com.tridya.ebookhaven.ui.home.adapter.HomeAdapter
import com.tridya.ebookhaven.utils.FindAuthor
import com.tridya.ebookhaven.utils.FindCover
import com.tridya.ebookhaven.utils.FindTitle
import com.tridya.ebookhaven.utils.gone
import com.tridya.ebookhaven.utils.visible
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : BaseFragment(), HomeAdapter.onItemClick {
    private lateinit var binding: FragmentHomeBinding

    private val adapter by lazy { HomeAdapter(listener = this) }
    private var findTitle = FindTitle()
    private var findAuthor = FindAuthor()
    private var findCover = FindCover()
    private var bookList: ArrayList<BookInfo>? = null

    private var REQUEST_PERMISSIONS = 1
    var file: File? = null
    private var fileImages: File? = null

    private var fileOutputStream: FileOutputStream? = null
    private var fileOutputStreamImages: FileOutputStream? = null
    private var writer: OutputStreamWriter? = null

    private var bitmap: Bitmap? = null
    private var layoutManager: LinearLayoutManager? = null
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

        checkPermissions()
        binding.toolBar.tvTitle.text = getString(R.string.recent_books)

        binding.toolBar.ivFavorites.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("showFavorite", true)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            checkPermissionForAndroid11AndAbove()
        } else if (!storagePermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS
            )
        } else {
            val epubFiles = getEpubFiles()
            Log.e("EPUB File list", "LISTofEPUBfiles: $epubFiles")
            for (epub in epubFiles) {
                addEpubBookDetails(epub)
            }
            layoutManager = LinearLayoutManager(requireContext())
            binding.rvBooks.layoutManager = layoutManager
            binding.rvBooks.adapter = adapter
            if (bookList == null) {
                binding.rvBooks.gone()
                binding.llNothingError.visible()
            } else {
                adapter.clearList()
                adapter.setList(bookList)
            }
        }
    }

    /*override fun onBookSelected(book: Book) {
        BookStore.setBook(book)
//        val intent = Intent(activity, BookInfoActivity::class.java)
//        intent.putExtra("BookId", book.id)
//        intent.putExtra("BookTitle",book.title)
//        intent.putExtra("BookEpubFile", book.formats.applicationepubzip)
//        intent.putExtra("Book",book)
//        this.startActivity(intent)
    }
    */
    private fun storagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        checkPermissionForAndroid11AndAbove()
                        return
                    }
                }
//                ListBooksNCheckPreferences()
                val epubFiles = getEpubFiles()
                Log.e("EPUB File list", "onRequestPermissionsResult: $epubFiles")
            } else {
                showToastShort("Permission not Granted!!")
//                finish()
            }
        }
    }

    private fun checkPermissionForAndroid11AndAbove() {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                startActivity(intent)
            }
        } catch (ex: Exception) {
            showToastShort("Permission not Granted!!")
//            finish()
        }
    }

    private fun getEpubFiles(): List<File> {
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val epubFiles = mutableListOf<File>()

        if (externalStorageDirectory != null && externalStorageDirectory.exists()) {
            scanForEpubFiles(externalStorageDirectory, epubFiles)
        } else {
            showToastShort("Permission not Granted!!")
        }
        return epubFiles
    }

    private fun scanForEpubFiles(directory: File, epubFiles: MutableList<File>) {
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    // Recursively search directories
                    scanForEpubFiles(file, epubFiles)
                } else {
                    if (file.name.endsWith(".epub", ignoreCase = true)) {
                        // If the file has the .epub extension, add it to the list
                        epubFiles.add(file)
//                        addEpubBookDetails(file)
                    }
                }
            }
        }
    }

    private fun addEpubBookDetails(fileEpub: File) {
        try {
            file = File(requireActivity().filesDir, "bookList.txt")
            fileImages = File(requireActivity().filesDir.toString() + File.separator + "bookImages")
            if (!file!!.exists()) {
                file!!.createNewFile()
            }
            fileImages!!.mkdirs()

            fileOutputStream = FileOutputStream(file, false)
            writer = OutputStreamWriter(fileOutputStream)

            bitmap = null

            findTitle = FindTitle()
            val title: String = findTitle.FindTitle1(fileEpub.absolutePath)
            val author: String = findAuthor.FindAuthor1(fileEpub.absolutePath)
            val imageName = "$title.jpeg"
            var imageItem = File(fileImages, imageName)
            if (!imageItem.exists()) {
                bitmap = findCover.FindCoverRef(fileEpub.absolutePath) as Bitmap
                if (bitmap != null) {
                    fileOutputStreamImages = FileOutputStream(imageItem)
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStreamImages!!)
                } else {
                    val fileNull = File("null")
                    imageItem = fileNull
                }
                val importTime = SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.US
                ).format(Calendar.getInstance().time)
                val bookInfo =
                    BookInfo(title, author, imageItem, fileEpub.absolutePath, importTime, 0, 0, 0)
                Log.e("EPUB File list", "This is book info: $bookInfo")
                if (fileOutputStreamImages != null) {
                    fileOutputStreamImages!!.flush()
                    fileOutputStreamImages!!.close()
                }
                bookList?.add(bookInfo)
                Log.e("TAG= list data", "addEpubBookDetails: $bookList. ")
            }
        } catch (e: Exception) {
            Log.e("TAG", "addEpubBookDetails: ${e.message}")
        }

    }

    override fun onBookSelected(bookInfo: BookInfo) {

    }

}