package com.tridya.ebookhaven.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.database.table.BookListModel
import com.tridya.ebookhaven.databinding.ItemDownloadedBookBinding
import com.tridya.ebookhaven.utils.GlideUtils
import java.io.File
import java.text.CharacterIterator
import java.text.DateFormat
import java.text.StringCharacterIterator
import java.util.Date
import java.util.Locale

class LibraryAdapter(
    private val list: ArrayList<BookListModel> = arrayListOf(),
    val listener: onItemClick,
) :
    RecyclerView.Adapter<LibraryAdapter.HomeViewHolder>() {
    class HomeViewHolder(val binding: ItemDownloadedBookBinding, val mContext: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BookListModel) {
            GlideUtils(mContext).cornerRadius(16)
                .loadImageLibrary(data.imagejpeg, binding.imgCover)
            /*     Picasso.get().load(data.formats.imagejpeg)
                     .error(R.drawable.ic_book_black_24dp).into( binding.imgCover)*/

            binding.tvTitle.text = data.bookTitle
            binding.tvAuthor.text = data.bookAuthor
            binding.tvBookSize.text =
                binding.root.context.getString(
                    R.string.booksize,
                    getFileSize(data.bookPath.toString())
                )
            binding.tvDate.text = getDownloadDate(data.createdAt)

        }

        private fun getFileSize(filePath: String): String {
            val file = File(filePath)
            if (file.exists()) {
                var bytes = file.length()
                if (-1000 < bytes && bytes < 1000) {
                    return "$bytes B"
                }
                val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
                while (bytes <= -999950 || bytes >= 999950) {
                    bytes /= 1000
                    ci.next()
                }
                return java.lang.String.format(Locale.US, "%.1f %cB", bytes / 1000.0, ci.current())
            }
            return ""
        }

        private fun getDownloadDate(createdAt: Long): String {
            val date = Date(createdAt)
            return DateFormat.getDateInstance().format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemDownloadedBookBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HomeViewHolder(binding, parent.context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)
        holder.binding.root.setOnClickListener {
            listener.onBookSelected(list[position])
        }
    }

    interface onItemClick {
        fun onBookSelected(book: BookListModel)
    }

}