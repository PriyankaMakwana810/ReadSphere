package com.tridya.readsphere.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.ActionMode
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tridya.readsphere.R
import com.tridya.readsphere.adapters.ChaptersAdapter
import com.tridya.readsphere.adapters.HighlightAdapter
import com.tridya.readsphere.base.BaseActivity
import com.tridya.readsphere.database.Database
import com.tridya.readsphere.database.dao.BookListDao
import com.tridya.readsphere.database.table.BookListModel
import com.tridya.readsphere.databinding.ActivityEpubViewerBinding
import com.tridya.readsphere.databinding.BottomSheetAdvanceSettingsBinding
import com.tridya.readsphere.databinding.BottomSheetChaptersBinding
import com.tridya.readsphere.databinding.BottomSheetFontSelectionBinding
import com.tridya.readsphere.databinding.BottomSheetHighlightsBinding
import com.tridya.readsphere.databinding.BottomSheetReadingOptionsBinding
import com.tridya.readsphere.databinding.BottomSheetSettingsBinding
import com.tridya.readsphere.databinding.DialogLockBookBinding
import com.tridya.readsphere.databinding.DialogUnlockBookBinding
import com.tridya.readsphere.models.UserPref
import com.tridya.readsphere.utils.CustomWebView
import com.tridya.readsphere.utils.Session
import com.tridya.readsphere.utils.gone
import com.tridya.readsphere.utils.visible
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.Calendar
import java.util.Objects
import android.view.View as View1


class EpubViewer : BaseActivity(), ChaptersAdapter.OnItemClickListener,
    HighlightAdapter.OnHighlightClickListener {
    private lateinit var binding: ActivityEpubViewerBinding
    private lateinit var settingsBinding: BottomSheetSettingsBinding
    private lateinit var advanceSettingsBinding: BottomSheetAdvanceSettingsBinding
    private lateinit var chaptersBinding: BottomSheetChaptersBinding
    private lateinit var fontSelectionBinding: BottomSheetFontSelectionBinding
    private lateinit var readingOptionsBinding: BottomSheetReadingOptionsBinding
    private lateinit var highlightsBinding: BottomSheetHighlightsBinding

    var session: Session? = null
    lateinit var db: Database
    lateinit var bookListDao: BookListDao
    var context: Context? = null
    var sharedPreferences: SharedPreferences? = null
    lateinit var webView: CustomWebView

    var targetPage = 0
    var saveQuote: SaveQuote? = null
    private var quoteList: MutableList<List<*>> = ArrayList()
    private var bookQuoteList: MutableList<String> = ArrayList()
    private var bookTitle: String? = null
    var gQuote = ""
    private var searchViewLongClick = false
    var seeking = false

    var autoScrollHandler = Handler()
    var currentScrollSpeed = 1
    private var bookId = 0

    var path: String? = null
    var title: String? = null
    private var author: String? = null
    private var bookCoverImage: String? = null
    private var unzipEpub: UnzipEpub? = null
    private var pagesRef: List<String> = ArrayList()
    var pages: List<String> = ArrayList()
    var pageNumber = 0

    private var dialogChapterBottomSheet: BottomSheetDialog? = null
    private var dialogHighlightsBottomSheet: BottomSheetDialog? = null
    private var dialogSettingsBottomSheet: BottomSheetDialog? = null
    private var dialogFontsBottomSheet: BottomSheetDialog? = null
    private var dialogReadingOpnBottomSheet: BottomSheetDialog? = null
    private var dialogAdvanceSettingsBottomSheet: BottomSheetDialog? = null

    private var switchState = true
    var userPref: UserPref? = null

    var webViewScrollAmount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext

        binding = ActivityEpubViewerBinding.inflate(layoutInflater)

        initBinding()
        setContentView(binding.root)

        session = Session(this)

        val ROOM_DB = "book-database"
        db = Room.databaseBuilder(
            applicationContext, Database::class.java, ROOM_DB
        ).allowMainThreadQueries().build()
        bookListDao = db.bookDao()

        if (session?.isLoggedIn == true) {
            userPref = session?.userPref
        } else {
            userPref = UserPref()
        }
        // web view Settings, inject css and javascript for all epub settings
        loadWebViewSettings()
        //Save Quotes (load highlighted quotes)
        try {
            saveQuote = SaveQuote(webView, quoteList)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        seekbarChangeListener()

        //Unzip and Show Epub
        path = intent.getStringExtra("path")
        title = intent.getStringExtra("title")
        author = intent.getStringExtra("author")
        bookCoverImage = intent.getStringExtra("bookCoverImage")
        bookTitle = intent.getStringExtra("title")
        val bookLanguage = intent.getStringExtra("bookLanguage")
        val bookPublisher = intent.getStringExtra("publisher")
        bookId = intent.getIntExtra("BookId", 0)
        binding.tvBookHeading.text = bookTitle

        unzipEpub = UnzipEpub(context, pagesRef, pages)
        unzipEpub!!.Unzip(path)
        if (pages.isNotEmpty()) {
            if (bookListDao.isBookOpened(bookTitle)) {
                if (bookListDao.getLastOpenedPage(bookTitle) != null) {
                    pageNumber = bookListDao.getLastOpenedPage(bookTitle)
                    userPref!!.currentPage = pageNumber
                    session?.userPref = userPref
                } else {
                    pageNumber = 0
                }
                if (bookListDao.getLastOpenedPosition(bookTitle) != null) {
                    webViewScrollAmount = bookListDao.getLastOpenedPosition(bookTitle)
                }
            }
            val isFavoirte = bookListDao.isBookFavorite(title)
            val bookData = bookListDao.getBookData(title)
            if (bookData != null) {
                bookListDao.updateBook(
                    BookListModel(
                        null,
                        title,
                        author,
                        path,
                        bookLanguage,
                        bookPublisher,
                        pages.size,
                        false,
                        null,
                        true,
                        pageNumber,
                        webView.scrollY,
                        isFavoirte,
                        bookCoverImage,
                        System.currentTimeMillis()
                    )
                )
            } else {
                bookListDao.insertBook(
                    BookListModel(
                        null,
                        title,
                        author,
                        path,
                        bookLanguage,
                        bookPublisher,
                        pages.size,
                        false,
                        null,
                        true,
                        pageNumber,
                        webView.scrollY,
                        isFavoirte,
                        bookCoverImage,
                        System.currentTimeMillis()
                    )
                )
            }
            webView.loadUrl("file://" + pages[pageNumber])
        } else {
            finish()
            Toast.makeText(context, "Unable to open", Toast.LENGTH_LONG).show()
        }
        if (bookListDao.isBookLock(bookTitle)) {
            showEnterPasswordDialog()
        }

        //Save Quotes Get Quotes
        try {
            saveQuote!!.getQuotes(bookTitle)
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        settings = findViewById(R.id.imgSettings)
//        bottomSheetViewInitialization()
        binding.imgSettings.setOnClickListener {
            dialogSettingsBottomSheet!!.show()
            val currentBrightness =
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
            // Map the current brightness to SeekBar progress
            val initialProgressBrightness = currentBrightness * 100 / 255
            settingsBinding.seekBarBrightness.progress = initialProgressBrightness
            settingsBinding.seekBarBrightness.setOnSeekBarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // Calculate the brightness value based on SeekBar progress
                    val brightness = progress * 255 / 100
                    // Apply the calculated brightness value
                    setBrightness(brightness)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Not needed for this example
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Not needed for this example
                }
            })

            val fontSizePercentages = arrayOf("90%", "95%", "100%", "105%", "110%")
            if (userPref?.fontSize?.equals(null) == true) {
                val initialProgress =
                    listOf(*fontSizePercentages).indexOf("100%") * 100 / (fontSizePercentages.size - 1)
                settingsBinding.seekBarFont.progress = initialProgress
            } else {
                val initialProgress =
                    listOf(*fontSizePercentages).indexOf(userPref?.fontSize) * 100 / (fontSizePercentages.size - 1)
                settingsBinding.seekBarFont.progress = initialProgress
            }
            settingsBinding.seekBarFont.setOnSeekBarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // Calculate the index in the fontSizePercentages array
                    val index = progress * (fontSizePercentages.size - 1) / 100

                    // Get the corresponding font size percentage from the array
                    val fontSizePercentage = fontSizePercentages[index]
                    userPref!!.fontSize = fontSizePercentage
                    session?.userPref = userPref
//                    sessionManager!!.saveUser(userPref)
                    //                    setToPreferences("font-size", fontSizePercentage);
                    webView.reload()
                }


                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Not needed for this example
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Not needed for this example
                }
            })
            settingsBinding.bgWhite.setOnClickListener {
                userPref!!.backgroundColor = "White"
                userPref!!.fontColor = "Black"
//                sessionManager!!.saveUser(userPref)
                session?.userPref = userPref
                webView.reload()
                dialogSettingsBottomSheet!!.dismiss()
            }
            settingsBinding.bgGray.setOnClickListener {
                userPref!!.backgroundColor = "Gray"
                userPref!!.fontColor = "White"
//                sessionManager!!.saveUser(userPref)
                session?.userPref = userPref
                webView.reload()
                dialogSettingsBottomSheet!!.dismiss()
            }
            settingsBinding.bgBlack.setOnClickListener {
                userPref!!.backgroundColor = "Black"
                userPref!!.fontColor = "White"
//                sessionManager!!.saveUser(userPref)
                session?.userPref = userPref
                webView.reload()
                dialogSettingsBottomSheet!!.dismiss()
            }

            settingsBinding.btnFontSelection.setOnClickListener {
                dialogSettingsBottomSheet!!.dismiss()

                dialogFontsBottomSheet!!.show()

                fontSelectionBinding.tvFontSansSerif.setOnClickListener {
//                    setToPreferences("font-family", "sans-serif");
                    userPref!!.font = "sans-serif"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref
                    webView.reload()
                    dialogFontsBottomSheet!!.dismiss()
                }
                fontSelectionBinding.tvFontSerif.setOnClickListener {
//                    setToPreferences("font-family", "serif");
                    userPref!!.font = "serif"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref
                    webView.reload()
                    dialogFontsBottomSheet!!.dismiss()
                }
                fontSelectionBinding.tvFontMonospace.setOnClickListener {
//                    setToPreferences("font-family", "monospace");
                    userPref!!.font = "monospace"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref
                    webView.reload()
                    dialogFontsBottomSheet!!.dismiss()
                }
                fontSelectionBinding.tvFontCursive.setOnClickListener {
//                    setToPreferences("font-family", "cursive");
                    userPref!!.font = "cursive"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref
                    webView.reload()
                    dialogFontsBottomSheet!!.dismiss()
                }
                fontSelectionBinding.tvFontDefault.setOnClickListener {
//                    setToPreferences("font-family", "default");
                    userPref!!.font = "default"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref
                    webView.reload()
                    dialogFontsBottomSheet!!.dismiss()
                }
            }
            settingsBinding.btnReadingOptions.setOnClickListener {
                dialogSettingsBottomSheet!!.dismiss()

                dialogReadingOpnBottomSheet!!.show()

                //                    String isSwipe = getFromPreferences("isSwipe");
                readingOptionsBinding.switchReadingMode.isChecked =
                    java.lang.Boolean.TRUE != session?.userPref?.readingModeSwipe
                if (readingOptionsBinding.switchReadingMode.isChecked) {
                    readingOptionsBinding.tvSwitchYes.setTextColor(
                        ContextCompat.getColor(
                            this, R.color.theme_primary
                        )
                    )
                    readingOptionsBinding.tvSwitchNo.setTextColor(
                        ContextCompat.getColor(
                            this, R.color.white
                        )
                    )
                } else {
                    readingOptionsBinding.tvSwitchYes.setTextColor(
                        ContextCompat.getColor(
                            this, R.color.white
                        )
                    )
                    readingOptionsBinding.tvSwitchNo.setTextColor(
                        ContextCompat.getColor(
                            this, R.color.theme_primary
                        )
                    )
                }
                readingOptionsBinding.switchReadingMode.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    if (isChecked) {
                        readingOptionsBinding.tvSwitchYes.setTextColor(
                            ContextCompat.getColor(
                                this, R.color.theme_primary
                            )
                        )
                        readingOptionsBinding.tvSwitchNo.setTextColor(
                            ContextCompat.getColor(
                                this, R.color.white
                            )
                        )
                        //                            setToPreferences("isSwipe", "false");
//                            sharedPreferences.edit().putBoolean("isSwipe", false).apply();
                        userPref!!.readingModeSwipe = false
//                        sessionManager!!.saveUser(userPref)
                        session?.userPref = userPref
                    } else {
                        readingOptionsBinding.tvSwitchYes.setTextColor(
                            ContextCompat.getColor(
                                this, R.color.white
                            )
                        )
                        readingOptionsBinding.tvSwitchNo.setTextColor(
                            ContextCompat.getColor(
                                this, R.color.theme_primary
                            )
                        )
                        userPref!!.readingModeSwipe = true
                        session?.userPref = userPref
                    }
                }
                readingOptionsBinding.switchAutomaticScrolling.isChecked = false
                readingOptionsBinding.switchAutomaticScrolling.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    if (isChecked) {
                        dialogReadingOpnBottomSheet!!.dismiss()
                        // Start automatic scrolling
                        startAutoScroll(webView, binding.seekBarAutoScrollSpeed.progress)
                        webView.isEnabled = false
                        binding.toolbar.visibility = View1.GONE
                        binding.seekLayout.visibility = View1.GONE
                        binding.llAutoScroll.visibility = View1.VISIBLE
                    } else {
                        // Stop automatic scrolling
                        stopAutoScroll()
                        webView.isEnabled = true
                        binding.toolbar.visibility = View1.VISIBLE
                        binding.seekLayout.visibility = View1.VISIBLE
                        binding.llAutoScroll.visibility = View1.GONE
                    }
                }
            }
            settingsBinding.btnMyMarkings.setOnClickListener {
                dialogSettingsBottomSheet!!.dismiss()
                val highlightAdapter = HighlightAdapter(bookQuoteList, pageNumber)
                highlightAdapter.setOnItemClickListener(this)
                highlightsBinding.rvMarkings.adapter = highlightAdapter
                dialogHighlightsBottomSheet!!.show()
                highlightsBinding.ivDeleteAll.setOnClickListener {

                    val parentFile =
                        File(
                            Objects.requireNonNull(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            ).absolutePath + File.separator + "eBookHaven Book Quotes"
                        )
                    val file = File(parentFile, "$bookTitle Quote List.txt")
                    Log.e("TAG", "onCreate:Delete File Path is:  $file")
                    dialogHighlightsBottomSheet!!.dismiss()
                    deleteRecursive(file)
                    quoteList.clear()
                    bookQuoteList.clear()
                    highlightAdapter.notifyDataSetChanged()
                    reloadNavQuote()
                    webView.reload()
                }
            }

            settingsBinding.btnAdvanceSettings.setOnClickListener {
                dialogSettingsBottomSheet!!.dismiss()
                dialogAdvanceSettingsBottomSheet!!.show()

                advanceSettingsBinding.switchScreenRotation.isChecked = switchState

                val savedLineHeight = session?.userPref?.lineSpacing
                val savedWordSpacing = session?.userPref?.wordSpacing
                val savedSideMargin = session?.userPref?.sideSpacing

                val lineSpacingValues = arrayOf("1", "1.5", "2", "2.5", "3")
                val wordSpacingValues = arrayOf("normal", "0.1em", "0.2em", "0.3em", "0.4em")
                val sideMarginValues = arrayOf("10px", "15px", "20px", "25px", "30px")

                val savedLineHeightIndex = listOf(*lineSpacingValues).indexOf(savedLineHeight)
                val savedWordSpacingIndex = listOf(*wordSpacingValues).indexOf(savedWordSpacing)
                val savedSideMarginIndex = listOf(*sideMarginValues).indexOf(savedSideMargin)

                advanceSettingsBinding.seekBarLineSpacing.max = lineSpacingValues.size - 1
                advanceSettingsBinding.seekBarWordSpacing.max = wordSpacingValues.size - 1
                advanceSettingsBinding.seekBarSpacingFromSide.max = sideMarginValues.size - 1

                advanceSettingsBinding.seekBarLineSpacing.progress = savedLineHeightIndex
                advanceSettingsBinding.seekBarWordSpacing.progress = savedWordSpacingIndex
                advanceSettingsBinding.seekBarSpacingFromSide.progress = savedSideMarginIndex

                advanceSettingsBinding.switchBookLock.isChecked = bookListDao.isBookLock(bookTitle)
                advanceSettingsBinding.switchBookLock.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    if (isChecked) {
                        showLockBookDialog()
                        dialogAdvanceSettingsBottomSheet!!.dismiss()
                    } else {
//                        sharedPreferences.edit().putBoolean("isBookLocked", false).apply();
                        userPref!!.isBookLock = false
//                        sessionManager!!.saveUser(userPref)
                        session?.userPref = userPref
                        if (bookListDao.isBookLock(bookTitle)) {
                            bookListDao.updateIsBookLock(false, bookTitle)
                            Toast.makeText(
                                this, "book password removed successfully!! ", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                advanceSettingsBinding.switchBrowsing.isChecked =
                    java.lang.Boolean.TRUE == session?.userPref?.isBrowsingByMargins
                advanceSettingsBinding.switchBrowsing.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    // Enable or disable the SeekBars based on the Switch state
                    advanceSettingsBinding.seekBarLineSpacing.isEnabled = isChecked
                    advanceSettingsBinding.seekBarWordSpacing.isEnabled = isChecked
                    advanceSettingsBinding.seekBarSpacingFromSide.isEnabled = isChecked
                    //                    setToPreferences("isMarginsSeekbarEnabled", String.valueOf(isChecked));
                    userPref!!.isBrowsingByMargins = isChecked
                    session?.userPref = userPref
//                    sessionManager!!.saveUser(userPref)
                }
                val isSwitchChecked = advanceSettingsBinding.switchBrowsing.isChecked
                advanceSettingsBinding.seekBarLineSpacing.isEnabled = isSwitchChecked
                advanceSettingsBinding.seekBarWordSpacing.isEnabled = isSwitchChecked
                advanceSettingsBinding.seekBarSpacingFromSide.isEnabled = isSwitchChecked
                advanceSettingsBinding.seekBarLineSpacing.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        // Get the corresponding line spacing value based on the progress
                        val lineSpacingValue = lineSpacingValues[progress]
                        // Save the selected line height value to bookPreferences
                        userPref!!.lineSpacing = lineSpacingValue
//                        sessionManager!!.saveUser(userPref)
                        session?.userPref = userPref
                        // Reload the WebView
                        webView.reload()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                advanceSettingsBinding.seekBarWordSpacing.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        // Get the corresponding word spacing value based on the progress
                        val wordSpacingValue = wordSpacingValues[progress]
                        // Save the selected word spacing value to bookPreferences
//                        setToPreferences("word-spacing", wordSpacingValue);
                        userPref!!.wordSpacing = wordSpacingValue
//                        sessionManager!!.saveUser(userPref)
                        session?.userPref = userPref
                        // Reload the WebView
                        webView.reload()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                advanceSettingsBinding.seekBarSpacingFromSide.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        val sideMarginValue = sideMarginValues[progress]
                        //                        setToPreferences("margin", sideMarginValue);
                        userPref!!.sideSpacing = sideMarginValue
//                        sessionManager!!.saveUser(userPref)
                        session?.userPref = userPref
                        webView.reload()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                advanceSettingsBinding.switchScreenRotation.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    requestedOrientation = if (isChecked) {
                        // Lock the screen orientation
                        ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    } else {
                        // Unlock the screen orientation
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    switchState = isChecked
                }
                if (savedInstanceState != null) {
                    switchState = savedInstanceState.getBoolean(SWITCH_STATE_KEY, false)
                    advanceSettingsBinding.switchScreenRotation.isChecked = switchState
                }
            }
            dialogSettingsBottomSheet!!.show()
        }

        binding.seekBarAutoScrollSpeed.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                updateAutoScrollSpeed(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnSwitchOffAutoScroll.setOnClickListener {
            stopAutoScroll()
            binding.toolbar.visibility = View1.VISIBLE
            binding.seekLayout.visibility = View1.VISIBLE
            binding.llAutoScroll.visibility = View1.GONE
            readingOptionsBinding.switchAutomaticScrolling.isChecked = false
            webView.isEnabled = true
        }
        binding.imgChapters.setOnClickListener {
            chaptersBinding.rvChapters.layoutManager = LinearLayoutManager(this)
            val adapter = ChaptersAdapter(pages)
            adapter.setOnItemClickListener(this)
            chaptersBinding.rvChapters.adapter = adapter
            dialogChapterBottomSheet!!.show()
        }
        binding.imgBookmark.setOnClickListener {
            highlightsBinding.rvMarkings.layoutManager = LinearLayoutManager(this)
            val highlightAdapter = HighlightAdapter(bookQuoteList, pageNumber)
            highlightAdapter.setOnItemClickListener(this)
            highlightsBinding.rvMarkings.adapter = highlightAdapter
            dialogHighlightsBottomSheet!!.show()
        }
        binding.imgInfo.setOnClickListener {
            val intent = Intent(this, BookInfoActivity::class.java)
            intent.putExtra("title", title)
//            intent.putExtra("BookEpubFile", book.formats.applicationepubzip)
            this.startActivity(intent)
        }
        binding.imgSearch.maxWidth = 800
        binding.imgSearch.setOnCloseListener { false }
        binding.imgSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                webView.findAllAsync(newText.trim { it <= ' ' })
                webView.findNext(true)
                return false
            }
        })
        binding.imgSearch.findViewById<View1>(androidx.appcompat.R.id.search_src_text)
            .setOnLongClickListener { v: View1? ->
                searchViewLongClick = true
                false
            }
        binding.tvBookHeading.setOnClickListener { finish() }
        for (i in pages.indices) {
            val firstSplittedLink =
                pages[i].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val secondSplittedLink =
                firstSplittedLink[firstSplittedLink.size - 1].split("\\.".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            /*binding.navViewContent.menu.add(secondSplittedLink[0])
            binding.navViewContent.menu.getItem(i).isCheckable = true
            binding.navViewContent.menu.getItem(i).setOnMenuItemClickListener { item: MenuItem ->
                for (i1 in pages.indices) {
                    val firstSplittedLink1 =
                        pages[i1].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val secondSplittedLink1 =
                        firstSplittedLink1[firstSplittedLink1.size - 1].split("\\.".toRegex())
                            .dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (secondSplittedLink1[0] == item.toString()) {
                        webView.loadUrl("file://" + pages[i1])
                        webViewScrollAmount = 0
                        break
                    }
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                false
            }*/
        }
//        navigationViewQuote = findViewById(R.id.nav_view_quote)
        /*shareQuotesButton = binding.navViewQuote.getHeaderView(0).findViewById(R.id.sharequotes)
        deleteQuotesButton = binding.navViewQuote.getHeaderView(0).findViewById(R.id.deletequotes)
        shareQuotesButton!!.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_SUBJECT, "$bookTitle Quote List")
            val parentFile =
                File(Environment.getExternalStorageDirectory(), "eBookHaven Book Quotes")
            val file = File(parentFile, "$bookTitle Quote List.txt")
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "$bookTitle Quote List"))
        }*/
        highlightsBinding.ivDeleteAll.setOnClickListener {
            val parentFile =
                File(Environment.getExternalStorageDirectory(), "eBookHaven Book Quotes")
            val file = File(parentFile, "$bookTitle Quote List.txt")
            if (file.exists()) {
                try {
                    Log.e("TAG", "onClick: $file")
                    if (file.delete()) {
                        showToastShort("HighlightedText of this Book deleted successfully")
                    } else {
                        showToastShort("HighlightedText of this Book can't delete.")
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.e("TAG", "onClick: ${exception.message}")
                }
            }
//            file.delete()
            quoteList.clear()
            bookQuoteList.clear()
            reloadNavQuote()
            webView.reload()
        }
        reloadNavQuote()
    }

    private fun initBinding() {
        settingsBinding = BottomSheetSettingsBinding.inflate(layoutInflater, binding.root, false)
        dialogSettingsBottomSheet = BottomSheetDialog(this)
        dialogSettingsBottomSheet!!.setContentView(settingsBinding.root)

        fontSelectionBinding =
            BottomSheetFontSelectionBinding.inflate(layoutInflater, binding.root, false)
        dialogFontsBottomSheet = BottomSheetDialog(this)
        dialogFontsBottomSheet!!.setContentView(fontSelectionBinding.root)

        readingOptionsBinding =
            BottomSheetReadingOptionsBinding.inflate(layoutInflater, binding.root, false)
        dialogReadingOpnBottomSheet = BottomSheetDialog(this)
        dialogReadingOpnBottomSheet!!.setContentView(readingOptionsBinding.root)

        highlightsBinding =
            BottomSheetHighlightsBinding.inflate(layoutInflater, binding.root, false)
        dialogHighlightsBottomSheet = BottomSheetDialog(this)
        dialogHighlightsBottomSheet!!.setContentView(highlightsBinding.root)

        advanceSettingsBinding =
            BottomSheetAdvanceSettingsBinding.inflate(layoutInflater, binding.root, false)
        dialogAdvanceSettingsBottomSheet = BottomSheetDialog(this)
        dialogAdvanceSettingsBottomSheet!!.setContentView(advanceSettingsBinding.root)

        chaptersBinding = BottomSheetChaptersBinding.inflate(layoutInflater, binding.root, false)
        dialogChapterBottomSheet = BottomSheetDialog(this)
        dialogChapterBottomSheet!!.setContentView(chaptersBinding.root)


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadWebViewSettings() {
        //WebView
        webView = binding.customWebView
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.defaultTextEncodingName = "utf-8"
//        webView.settings.builtInZoomControls = true
        webView.setGestureDetector(GestureDetector(CustomeGestureDetector()))
        webView.setOnTouchListener(object : OnTouchListener {
            val MAX_CLICK_DURATION = 100
            private var startClickTime: Long = 0
            override fun onTouch(v: View1, event: MotionEvent): Boolean {
                syncWebViewScrollSeekBar()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startClickTime = Calendar.getInstance().timeInMillis
                    }

                    MotionEvent.ACTION_UP -> {
                        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                        if (clickDuration < MAX_CLICK_DURATION) {
                            if (binding.seekLayout.visibility == View1.VISIBLE) {
                                binding.seekLayout.visibility = View1.GONE
                            } else if (binding.seekLayout.visibility == View1.GONE) {
                                binding.seekLayout.visibility = View1.VISIBLE
                            }
                            if (binding.toolbar.visibility == View1.VISIBLE) {
                                binding.toolbar.visibility = View1.GONE
                                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                            } else if (binding.toolbar.visibility == View1.GONE) {
                                binding.toolbar.visibility = View1.VISIBLE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                            }
                        }
                    }
                }
                return false
            }
        })
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!sharedPreferences!!.getBoolean("built_in_web_browser", false)) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                var url: String? = url
                super.onPageFinished(view, url)
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    return
                }
                webView.loadUrl("javascript:(function() { " + "var text=''; setInterval(function(){ if (window.getSelection().toString() && text!==window.getSelection().toString()){ text=window.getSelection().toString(); console.log(text); }}, 20);" + "})()")
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(
                        message: String,
                        lineNumber: Int,
                        sourceID: String,
                    ) {
                        gQuote = message
                    }
                }
                InjectCss(view, "::selection { background: #ffb7b7; }")
                InjectCss(
                    view,
                    "* { padding: 0px !important; letter-spacing: normal !important; max-width: none !important; }"
                )
                InjectCss(view, "* { font-family: " + session?.userPref?.font + " !important; }")
                //                InjectCss(view, "* @font-face { font-family: " + getFromPreferences("font-family") + "; src: url(\"file:///android_res/font/miriam_libre_regular.ttf\"); } body { font-family: " + getFromPreferences("font-family") + ", sans-serif; }");
                InjectCss(view, "* { font-size: " + session?.userPref?.fontSize + " !important; }")
                //                InjectCss(view, "* { font-style: " + getFromPreferences("font-style") + " !important; }");
                InjectCss(
                    view,
                    "* { font-weight: " + session?.userPref?.fontSize + " !important; }"
                )
                //                InjectCss(view, "* { text-align: " + getFromPreferences("text-align") + " !important; }");
                InjectCss(
                    view,
                    "body { background: " + session?.userPref?.backgroundColor + " !important; }"
                )
                InjectCss(view, "* { color: " + session?.userPref?.fontColor + " !important; }")
                InjectCss(
                    view,
                    "* { line-height: " + session?.userPref?.lineSpacing + " !important; }"
                )
                InjectCss(
                    view,
                    "* { word-spacing: " + session?.userPref?.wordSpacing + " !important; }"
                )
                InjectCss(
                    view,
                    "body { margin: " + session?.userPref?.sideSpacing + " !important; }"
                )
                InjectCss(
                    view,
                    "img { display: block !important; width: 100% !important; height: auto !important; }"
                )
                try {
                    url = URLDecoder.decode(url, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                if (pageNumber == pages.size) {
                    binding.llFinished.visible()
                } else {
                    binding.llFinished.gone()
                }
                for (i in pages.indices) {
                    assert(url != null)
                    if (url!!.contains(pages[i])) {
                        pageNumber = pages.indexOf(pages[i])
                        if (pageNumber > -1) {
                            val lastPage = (pages.size - 1).toString() + ""
                            binding.tvLastpage.text = lastPage
                            val pageNum = pageNumber.toString() + "/" + (pages.size - 1)
                            binding.tvCurrentPage.text = pageNum
                            if (!seeking) {
                                if (url.contains("#")) {
                                    val finalUrl = url
                                    webView.postDelayed({
                                        val anchor = finalUrl.split("#".toRegex())
                                            .dropLastWhile { it.isEmpty() }.toTypedArray()
                                        webView.loadUrl("javascript:document.getElementById(\"" + anchor[anchor.size - 1] + "\").scrollIntoView()")
                                        saveQuote!!.highlightQuote(pageNumber)
                                        syncWebViewScrollSeekBar()
                                    }, 500)
                                } else {
                                    webView.postDelayed({
                                        webView.scrollTo(0, webViewScrollAmount)
                                        saveQuote!!.highlightQuote(pageNumber)
                                        syncWebViewScrollSeekBar()
                                    }, 500)
                                }
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    private fun seekbarChangeListener() {
        //Seekbar
        binding.seekBar.max = 100
        binding.seekBar.setPadding(100, 0, 100, 0)
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var progress = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                this.progress = progress
                val strProgress = "$progress%"
                binding.textViewPercent.text = strProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (java.lang.Boolean.FALSE == session?.userPref?.readingModeSwipe) {
                    seeking = true
                    val whichPage = pages.size * progress.toFloat() / seekBar.max
                    val webViewHeight =
                        (webView.contentHeight * webView.scale).toInt() - webView.height
                    val fraction = whichPage - whichPage.toInt()
                    val whichScroll = (webViewHeight * fraction).toInt()
                    if (pages.size > whichPage) {
                        val targetPage = whichPage.toInt()
                        val animator = ValueAnimator.ofInt(webView.scrollY, whichScroll)
                        animator.duration = 500 // Duration of the smooth scroll in milliseconds
                        animator.addUpdateListener { animation: ValueAnimator ->
                            val scrollTo = animation.animatedValue as Int
                            webView.scrollTo(0, scrollTo)
                        }
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}
                            override fun onAnimationEnd(animation: Animator) {
                                // Load the new page and highlight the quote after scrolling
                                webView.loadUrl("file://" + pages[targetPage])
                                webView.postDelayed({
                                    saveQuote!!.highlightQuote(pageNumber)
                                    seeking = false
                                }, 500)
                            }

                            override fun onAnimationCancel(animation: Animator) {}
                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        animator.start()
                    }
                } else {
                    seeking = true
                    val whichPage = pages.size * progress.toFloat() / seekBar.max
                    targetPage = whichPage.toInt()
                    if (targetPage > pageNumber) {
                        // Swipe from left to right (go to next page)
                        swipeToPreviousPage()
                    } else if (targetPage < pageNumber) {
                        // Swipe from right to left (go to previous page)
                        swipeToNextPage()
                    }
                }
            }
        })
    }

    override fun onStop() {
        //save last opened state
        bookListDao.updateIsBookOpened(true, bookTitle)
        bookListDao.updateLastOpenedPage(pageNumber, webView.scrollY, bookTitle)
        super.onStop()
    }


    private fun deleteRecursive(fileOrDirectory: File) {
        //deletes all quotes (highlighted text of book)
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
//                setData()
        dialogHighlightsBottomSheet?.show()
        quoteList.clear()
        bookQuoteList.clear()

    }

    private fun showEnterPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogUnlockBookBinding = DialogUnlockBookBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogUnlockBookBinding.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialogUnlockBookBinding.ivPassVisible.setOnClickListener { v: View1? ->
            if (dialogUnlockBookBinding.passwordEditText.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                dialogUnlockBookBinding.passwordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                dialogUnlockBookBinding.passwordEditText.setSelection(dialogUnlockBookBinding.passwordEditText.length())
                dialogUnlockBookBinding.ivPassVisible.setImageResource(R.drawable.v_ic_hide_password) //visible
            } else {
                dialogUnlockBookBinding.passwordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                dialogUnlockBookBinding.passwordEditText.setSelection(dialogUnlockBookBinding.passwordEditText.length())
                dialogUnlockBookBinding.ivPassVisible.setImageResource(R.drawable.ic_pwd_hide) //hide
            }
        }
        dialogUnlockBookBinding.ivClose.setOnClickListener {
//            dialog.dismiss()
            finish()
        }
        dialogUnlockBookBinding.unlockBtn.setOnClickListener { v: View1? ->
            val enteredPassword =
                dialogUnlockBookBinding.passwordEditText.text.toString().trim { it <= ' ' }
            if (enteredPassword == session?.userPref?.bookPassword) {
                // Password is correct, you can proceed with app functionality
                dialog.dismiss()
            } else {
                // Password is incorrect, show an error message
                Toast.makeText(context, "Incorrect Password!!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showLockBookDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLockBookBinding = DialogLockBookBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogLockBookBinding.root)
        val dialog = builder.create()
        dialogLockBookBinding.ivPassVisible.setOnClickListener { v: View1? ->
            if (dialogLockBookBinding.passwordEditText.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                dialogLockBookBinding.passwordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                dialogLockBookBinding.passwordEditText.setSelection(dialogLockBookBinding.passwordEditText.length())
                dialogLockBookBinding.ivPassVisible.setImageResource(R.drawable.v_ic_hide_password) //visible
            } else {
                dialogLockBookBinding.passwordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                dialogLockBookBinding.passwordEditText.setSelection(dialogLockBookBinding.passwordEditText.length())
                dialogLockBookBinding.ivPassVisible.setImageResource(R.drawable.ic_pwd_hide) //hide
            }
        }
        dialogLockBookBinding.ivRePassVisible.setOnClickListener { v: View1? ->
            if (dialogLockBookBinding.repeatPasswordEditText.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                dialogLockBookBinding.repeatPasswordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                dialogLockBookBinding.repeatPasswordEditText.setSelection(dialogLockBookBinding.repeatPasswordEditText.length())
                dialogLockBookBinding.ivRePassVisible.setImageResource(R.drawable.v_ic_hide_password) //visible
            } else {
                dialogLockBookBinding.repeatPasswordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                dialogLockBookBinding.repeatPasswordEditText.setSelection(dialogLockBookBinding.repeatPasswordEditText.length())
                dialogLockBookBinding.ivRePassVisible.setImageResource(R.drawable.ic_pwd_hide) //hide
            }
        }
        dialogLockBookBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialogLockBookBinding.saveButton.setOnClickListener { v: View1? ->
            val password = dialogLockBookBinding.passwordEditText.text.toString().trim { it <= ' ' }
            val repeatPassword =
                dialogLockBookBinding.repeatPasswordEditText.text.toString().trim { it <= ' ' }
            if (password == repeatPassword) {
                userPref!!.isBookLock = true
                userPref!!.bookPassword = password
                session?.userPref = userPref
                bookListDao.updateIsBookLock(true, bookTitle)
                bookListDao.updateBookPassword(password, bookTitle)
                dialog.dismiss()
                finish()
                Toast.makeText(this, "book locked successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Passwords don't match, show an error message or handle as needed
                userPref!!.isBookLock = false
                session?.userPref = userPref
                bookListDao.updateIsBookLock(false, bookTitle)
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun swipeToNextPage() {
        val animationDuration = 100 // Duration of the animation in milliseconds
        // Calculate the target scroll position based on the seek bar progress
        val targetScrollY =
            (webView.contentHeight * webView.scale * (targetPage + 1).toFloat() / pages.size).toInt() - webView.height
        // Simulate a swipe effect (left to right)
        val animator = ObjectAnimator.ofFloat(webView, "translationX", -webView.width.toFloat(), 0f)
        animator.duration = animationDuration.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Set the new page number
                pageNumber = targetPage
                // Load the new page and highlight the quote after the animation
                webView.loadUrl("file://" + pages[pageNumber])
                webView.translationX = 0f // Reset translation
                scrollToPositionSmoothly(targetScrollY) // Smoothly scroll to the target position
                seeking = false
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private fun swipeToPreviousPage() {
        val animationDuration = 100 // Duration of the animation in milliseconds

        // Calculate the target scroll position based on the seek bar progress
        val targetScrollY =
            (webView.contentHeight * webView.scale * targetPage.toFloat() / pages.size).toInt() - webView.height

        // Simulate a swipe effect (right to left)
        val animator = ObjectAnimator.ofFloat(webView, "translationX", webView.width.toFloat(), 0f)
        animator.duration = animationDuration.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Set the new page number
                pageNumber = targetPage

                // Load the new page and highlight the quote after the animation
                webView.loadUrl("file://" + pages[pageNumber])
                webView.translationX = 0f // Reset translation
                scrollToPositionSmoothly(targetScrollY) // Smoothly scroll to the target position
                seeking = false
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private fun scrollToPositionSmoothly(targetScrollY: Int) {
//        final int animationDuration = 300; // Duration of the scroll animation in milliseconds
        val currentScrollY = webView.scrollY
        val animator = ObjectAnimator.ofInt(webView, "scrollY", currentScrollY, targetScrollY)
        animator.duration = 100
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Highlight the quote after scrolling to the target position
                saveQuote!!.highlightQuote(pageNumber)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SWITCH_STATE_KEY, switchState)
    }

    //Reload Navigation View Quote
    fun reloadNavQuote() {
        bookQuoteList.clear()
        for (i in quoteList.indices) {
            bookQuoteList.add(quoteList[i][0].toString())
        }
        if (quoteList.isEmpty()) {
            highlightsBinding.ivDeleteAll.visibility = View.GONE
        } else {
            highlightsBinding.ivDeleteAll.visibility = View.VISIBLE
        }

        if (quoteList.isEmpty()) {
            highlightsBinding.ivDeleteAll.visibility = View.GONE
        } else {
            highlightsBinding.ivDeleteAll.visibility = View.VISIBLE
        }
    }

    override fun onHighlightClick(position: Int) {
        for (j in quoteList.indices) {
            if (quoteList[j][0].toString() == position.toString()) {
                webView.loadUrl("file://" + pages[quoteList[j][2].toString().toInt()])
                webViewScrollAmount = quoteList[j][3].toString().toInt()
            }
        }
    }

    //On Activity Stop
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onItemClick(pageUrl: String?) {
//        webView.loadUrl("file://" + pages.get(i));
        webView.loadUrl("file://$pageUrl")
        webViewScrollAmount = 0
        dialogChapterBottomSheet!!.dismiss()
    }


    // Function to set screen brightness
    private fun setBrightness(brightness: Int) {
        // Apply brightness using Settings API
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness / 255f
        window.attributes = layoutParams
        userPref!!.brightness = brightness.toString()
//        sessionManager!!.saveUser(userPref)
        session?.userPref = userPref
    }

    //WebView LongClick Menu
    override fun onActionModeStarted(mode: ActionMode) {
        if (!searchViewLongClick) {
            mode.menu.add(0, 15264685, 0, "Highlight")
            mode.menu.add(0, 45657841, 0, "Remove Highlight")
            mode.menu.findItem(15264685).setOnMenuItemClickListener { item: MenuItem? ->
                try {
                    if (gQuote != "") {
                        saveQuote!!.addQuote(gQuote, bookTitle, pageNumber, webView.scrollY)
                        reloadNavQuote()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                false
            }
            mode.menu.findItem(45657841).setOnMenuItemClickListener { item: MenuItem? ->
                if (session?.userPref?.backgroundColor == null) {
                    userPref!!.backgroundColor = "White"
//                    sessionManager!!.saveUser(userPref)
                    session?.userPref = userPref

                }
                try {
                    if (gQuote != "") {
                        saveQuote!!.removeQuote(
                            gQuote,
                            bookTitle,
                            0,
                            session?.userPref?.backgroundColor
                        )
                        reloadNavQuote()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                false
            }
        }
        super.onActionModeStarted(mode)
    }

    override fun onActionModeFinished(mode: ActionMode) {
        searchViewLongClick = false
        super.onActionModeFinished(mode)
    }

    private fun startAutoScroll(webView: WebView?, initialSpeed: Int) {
        currentScrollSpeed = initialSpeed + 1 // Add 1 to avoid zero speed
        autoScrollHandler.postDelayed(object : Runnable {
            override fun run() {
                val currentScrollY = webView!!.scrollY
                webView.scrollTo(0, currentScrollY + currentScrollSpeed)
                autoScrollHandler.postDelayed(this, 10) // Adjust delay as needed
            }
        }, 10) // Initial delay before starting auto scroll
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacksAndMessages(null)
    }

    private fun updateAutoScrollSpeed(progress: Int) {
        currentScrollSpeed = progress + 1 // Add 1 to avoid zero speed
    }

    private fun InjectCss(webView: WebView, vararg cssRules: String) {
        val jsUrl = StringBuilder("javascript:")
        jsUrl.append(CREATE_CUSTOM_SHEET).append("if (typeof(customSheet) != 'undefined') {")
        var cnt = 0
        for (cssRule in cssRules) {
            jsUrl.append("customSheet.insertRule('").append(cssRule).append("', ").append(cnt++)
                .append(");")
        }
        jsUrl.append("}")
        webView.loadUrl(jsUrl.toString())
    }

    //Sync WebView Scroll and Seek Bar Progress
    fun syncWebViewScrollSeekBar() {
        if (webView.url != null && (webView.url!!.startsWith("http://") || webView.url!!.startsWith(
                "https://"
            ))
        ) {
            return
        }
        val real = binding.seekBar.max * pageNumber / pages.size
        val webViewHeight = webView.contentHeight * webView.scale - webView.height
        val partPerPage = (binding.seekBar.max / pages.size).toFloat()
        val fraction = webView.scrollY.toFloat() / webViewHeight * partPerPage
        binding.seekBar.progress = real + fraction.toInt()
    }

    //WebView Gesture swipe/scroll
    private inner class CustomeGestureDetector : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            return if (e1!!.pointerCount > 1 || e2.pointerCount > 1) false else {
                if (java.lang.Boolean.TRUE == session?.userPref?.readingModeSwipe) {
                    try {
                        // right to left swipe .. go to next page
                        if (e1.x - e2.x > 150 && Math.abs(velocityX) > 1000) {
                            if (pageNumber < pages.size - 1) {
                                pageNumber++
                                binding.llFinished.gone()
                                webView.loadUrl("file://" + pages[pageNumber])
                                binding.seekBar.progress =
                                    binding.seekBar.max * pageNumber / pages.size
                                webViewScrollAmount = 0
                            } else {
                                binding.llFinished.visible()
                                Handler(Looper.getMainLooper()).postDelayed({
//                                    binding.layoutRoot.visibility = View.INVISIBLE
                                    finish()
                                }, 1000)
                            }
                            return true
                        } else if (e2.x - e1.x > 150 && Math.abs(velocityX) > 1000) {
                            if (pageNumber > 0) {
                                pageNumber--
                                webView.loadUrl("file://" + pages[pageNumber])
                                binding.seekBar.progress =
                                    binding.seekBar.max * pageNumber / pages.size
                                webViewScrollAmount =
                                    (webView.contentHeight * webView.scale).toInt() - webView.height
                            }
                            return true
                        }
                    } catch (ignored: Exception) {
                    }
                } else {
                    try {//bottom to top, go to next document
                        if (e1.y - e2.y > 150 && Math.abs(velocityY) > 1000 && webView.scrollY >= Math.round(
                                webView.contentHeight * webView.scale
                            ) - webView.height - 10
                        ) {
                            if (pageNumber < pages.size - 1) {
                                pageNumber++
                                binding.llFinished.visibility = View1.GONE
                                webView.loadUrl("file://" + pages[pageNumber])
                                binding.seekBar.progress =
                                    binding.seekBar.max * pageNumber / pages.size
                                webViewScrollAmount = 0
                            } else {
                                binding.llFinished.visible()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    finish()
                                }, 1000)
                            }
                            return true
                        } //top to bottom, go to prev document
                        else if (e2.y - e1.y > 150 && Math.abs(velocityY) > 1000 && webView.scrollY <= 10) {
                            if (pageNumber > 0) {
                                pageNumber--
                                webView.loadUrl("file://" + pages[pageNumber])
                                binding.seekBar.progress =
                                    binding.seekBar.max * pageNumber / pages.size
                                webViewScrollAmount =
                                    (webView.contentHeight * webView.scale).toInt() - webView.height
                            }
                            return true
                        }
                    } catch (ignored: Exception) {
                    }
                }
                false
            }
        }
    }

    companion object {
        const val SWITCH_STATE_KEY = "switch_state_key"

        //Inject CSS to WebView
        private const val CREATE_CUSTOM_SHEET =
            "if (typeof(document.head) != 'undefined' && typeof(customSheet) == 'undefined') {" + "var customSheet = (function() {" + "var style = document.createElement(\"style\");" + "style.appendChild(document.createTextNode(\"\"));" + "document.head.appendChild(style);" + "return style.sheet;" + "})();" + "}"
    }
}