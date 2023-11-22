package com.tridya.readsphere.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tridya.readsphere.database.table.BookListModel
import com.tridya.readsphere.databinding.ItemBookBinding
import com.tridya.readsphere.utils.GlideUtils

class FavoritesAdapter(
    private val list: ArrayList<BookListModel> = arrayListOf(),
    val listener: onFavoriteItemClick,
) :
    RecyclerView.Adapter<FavoritesAdapter.HomeViewHolder>() {

    fun addList(list: List<BookListModel>?) {
        val prevSize = this.list.size
        val tempList = if (list.isNullOrEmpty()) arrayListOf() else list
        this.list.addAll(tempList)
        notifyItemRangeInserted(prevSize, this.list.size)
    }

    fun setList(list: List<BookListModel>?) {
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
        fun bind(data: BookListModel) {
            GlideUtils(mContext).cornerRadius(16)
                .loadImage(data.imagejpeg, binding.imgCover)
            /*     Picasso.get().load(data.formats.imagejpeg)
                     .error(R.drawable.ic_book_black_24dp).into( binding.imgCover)*/

            binding.tvTitle.text = data.bookTitle
            binding.tvAuthor.text = data.bookAuthor
            binding.tvLanguages.text = data.bookPublisher
            binding.tvLocation.text = data.bookLanguage
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
            listener.onFavoriteBookSelected(list[position])
        }
    }

    interface onFavoriteItemClick {
        fun onFavoriteBookSelected(bookData: BookListModel)
    }
}