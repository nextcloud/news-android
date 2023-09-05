package de.luhmer.owncloudnewsreader.adapter

interface RecyclerItemClickListener {
    fun onClick(vh: RssItemViewHolder<*>, position: Int)
    fun onLongClick(vh: RssItemViewHolder<*>, position: Int): Boolean
}
