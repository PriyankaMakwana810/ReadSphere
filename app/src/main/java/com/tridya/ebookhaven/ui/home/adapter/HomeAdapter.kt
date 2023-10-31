package com.tridya.ebookhaven.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.databinding.ItemBookBinding
import com.tridya.ebookhaven.models.book.BookInfo
import com.tridya.ebookhaven.utils.GlideUtils

class HomeAdapter(
    private val list: ArrayList<BookInfo> = arrayListOf(),
    val listener: onItemClick,
) :
    RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    fun addList(list: List<BookInfo>?) {
        val prevSize = this.list.size
        val tempList = if (list.isNullOrEmpty()) arrayListOf() else list
        this.list.addAll(tempList)
        notifyItemRangeInserted(prevSize, this.list.size)
    }

    fun setList(list: List<BookInfo>?) {
        clearList()
        val tempList = if (list.isNullOrEmpty()) arrayListOf() else list
        this.list.addAll(tempList)
        notifyDataSetChanged()
    }

    fun clearList() {
        this.list.clear()
        notifyDataSetChanged()
    }

    class HomeViewHolder(val binding: ItemBookBinding, val mContext: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BookInfo) {
            GlideUtils(mContext).cornerRadius(16)
                .loadImage(data.bookCover,binding.imgCover)
            binding.tvTitle.text = data.bookTitle
            binding.tvAuthor.text = data.bookAuthor
            binding.tvLanguages.text = data.bookLanguage
            binding.tvLocation.text = binding.root.context.getString(R.string.publisher, data.publisher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemBookBinding.inflate(
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
        fun onBookSelected(bookInfo: BookInfo)
    }
}