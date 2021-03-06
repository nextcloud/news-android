package de.luhmer.owncloudnewsreader.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mDivider;

    private int size = 0;

    public DividerItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
        if(mDivider != null)
            this.size = mDivider.getIntrinsicHeight();
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null || parent.getChildLayoutPosition(view) < 1) {
            return;
        }

        outRect.top = size;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(mDivider == null)
            return;

        if (!(parent.getLayoutManager() instanceof LinearLayoutManager) ||
                ((LinearLayoutManager)parent.getLayoutManager()).getOrientation() != RecyclerView.VERTICAL) {
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