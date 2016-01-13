package de.luhmer.owncloudnewsreader.adapter;

public interface RecyclerItemClickListener {
    void onClick(ViewHolder vh, int position);
    boolean onLongClick(ViewHolder vh, int position);
}
