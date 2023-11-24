package com.tridya.readsphere.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.tridya.readsphere.R
import com.tridya.readsphere.database.table.Quote

class HighlightAdapter(private val highlights: List<Quote>, private val pageNumber: Int) :
    RecyclerView.Adapter<HighlightAdapter.ViewHolder?>() {
    //    var pageNumber = 0
    private var clickListener: OnHighlightClickListener? = null

    interface OnHighlightClickListener {

        fun onHighlightClick(position: Int)
        fun onDeleteHighlightClicked(position: Int, highlight: Quote)
    }

    fun setOnItemClickListener(listener: OnHighlightClickListener?) {
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_highlights, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val highlight = highlights[position]
        holder.titleTextView.text = highlight.quoteText
        holder.ivDelete.setOnClickListener {
            clickListener?.onDeleteHighlightClicked(position,highlight)
        }
    }

    override fun getItemCount(): Int {
        return highlights.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView
        val ivDelete: AppCompatImageView
        init {
            titleTextView = itemView.findViewById(R.id.tvHighlight)
            ivDelete = itemView.findViewById(R.id.ivDelete)
            itemView.setOnClickListener { v: View? ->
                if (clickListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener!!.onHighlightClick(position)
                    }
                }
            }
        }
    }
}