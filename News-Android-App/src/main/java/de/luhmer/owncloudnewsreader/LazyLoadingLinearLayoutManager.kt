package de.luhmer.owncloudnewsreader

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class LazyLoadingLinearLayoutManager(
    context: Context?,
    @RecyclerView.Orientation orientation: Int,
    reverseLayout: Boolean,
) : LinearLayoutManager(context, orientation, reverseLayout) {
    var totalItemCount: Int = 0

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        if (state.itemCount == 0) {
            return 0
        }

        return (
            super.computeVerticalScrollRange(
                state,
            ) / state.itemCount.toFloat() * totalItemCount
        ).roundToInt()
    }
}
