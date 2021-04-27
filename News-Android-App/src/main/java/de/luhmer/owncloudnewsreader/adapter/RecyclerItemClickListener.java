package de.luhmer.owncloudnewsreader.adapter;

public interface RecyclerItemClickListener {
    void onClick(RssItemViewHolder vh, int position);

    boolean onLongClick(RssItemViewHolder vh, int position);
}
