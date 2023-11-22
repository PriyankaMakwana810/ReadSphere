package com.tridya.readsphere.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tridya.readsphere.R

class HighlightAdapter(private val highlightes: List<String>, private val pageNumber: Int) :
    RecyclerView.Adapter<HighlightAdapter.ViewHolder?>() {
    //    var pageNumber = 0
    private var clickListener: OnHighlightClickListener? = null

    interface OnHighlightClickListener {
        fun onHighlightClick(position: Int)
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
        val highlight = highlightes[position]
        holder.titleTextView.text = highlight
    }

    override fun getItemCount(): Int {
        return highlightes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView
        var pageNumTextView: TextView

        init {
            titleTextView = itemView.findViewById(R.id.tvHighlight)
            pageNumTextView = itemView.findViewById(R.id.tvPageNum)
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