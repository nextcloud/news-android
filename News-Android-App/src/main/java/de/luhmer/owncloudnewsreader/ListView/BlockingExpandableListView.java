package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class BlockingExpandableListView extends ExpandableListView {

    private boolean mBlockLayoutChildren;

    public BlockingExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public void setBlockLayoutChildren(boolean block) {
        mBlockLayoutChildren = block;
    }
 
    @Override
    protected void layoutChildren() {
        if (!mBlockLayoutChildren) {
            super.layoutChildren();
        }
    }    
}
