package com.tridya.readsphere.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tridya.readsphere.R

class ChaptersAdapter(private val pages: List<String>) :
    RecyclerView.Adapter<ChaptersAdapter.ViewHolder?>() {
    var pageNumber = 0
    private var clickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onChapterItemClick(pageUrl: String?)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chapters, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        pageNumber = pages.indexOf(pages[position])
        val fullPath = pages[position]
        val firstSplittedLink =
            pages[position].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val secondSplittedLink =
            firstSplittedLink[firstSplittedLink.size - 1].split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val pathSegments =
            fullPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val filename = pathSegments[pathSegments.size - 1]
        holder.titleTextView.text = secondSplittedLink[0]
        holder.pageNumTextView.text = pageNumber.toString() + ""
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView
        var pageNumTextView: TextView

        init {
            titleTextView = itemView.findViewById(R.id.tvChapter)
            pageNumTextView = itemView.findViewById(R.id.tvPageNum)
            itemView.setOnClickListener { v: View? ->
                if (clickListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val pageUrl = pages[position]
                        clickListener!!.onChapterItemClick(pageUrl)
                    }
                }
            }
        }
    }
}