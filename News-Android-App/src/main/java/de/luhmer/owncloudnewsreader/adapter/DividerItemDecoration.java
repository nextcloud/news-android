package de.luhmer.owncloudnewsreader.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    private int size = 0;

    public DividerItemDecoration(Context context) {
        final TypedArray a = context
                .obtainStyledAttributes(new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
        if(mDivider != null)
            this.size = mDivider.getIntrinsicHeight();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null || parent.getChildLayoutPosition(view) < 1) {
            return;
        }

        outRect.top = size;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if(mDivider == null)
            return;

        if (!(parent.getLayoutManager() instanceof LinearLayoutManager) ||
                ((LinearLayoutManager)parent.getLayoutManager()).getOrientation() != LinearLayoutManager.VERTICAL) {
            throw new IllegalStateException(
                    "DividerItemDecoration can only be used with a vertical LinearLayoutManager.");
        }

        Rect dividerRect = new Rect(0,0,0,0);
        int childCount = parent.getChildCount();

        dividerRect.left = parent.getPaddingLeft();
        dividerRect.right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            dividerRect.top = child.getTop() - params.topMargin;
            dividerRect.bottom = dividerRect.top + size;

            if(i > 0) {
                mDivider.setBounds(dividerRect);
                mDivider.draw(c);
            }
        }
    }
}