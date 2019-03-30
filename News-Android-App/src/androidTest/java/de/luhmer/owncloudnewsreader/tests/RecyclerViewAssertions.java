package de.luhmer.owncloudnewsreader.tests;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class RecyclerViewAssertions implements ViewAssertion {

    private int mExpectedPos;

    public RecyclerViewAssertions(int expectedPos) {
        this.mExpectedPos = expectedPos;
    }

    @Override
    public void check(View view, NoMatchingViewException e) {
        RecyclerView recyclerView = (RecyclerView) view;
        LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

        if(firstVisiblePosition != mExpectedPos) {
            throw new RuntimeException("Wrong position! Expected: " + mExpectedPos + " but was: " + firstVisiblePosition);
        }
    }
}
