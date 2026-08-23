package de.luhmer.owncloudnewsreader.junit_tests;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.luhmer.owncloudnewsreader.PodcastFragmentActivity;
import de.luhmer.owncloudnewsreader.R;

/**
 * Verifies that the layouts keep clear of a navigation bar at the side of the screen, which is where
 * it ends up in landscape with 3-button navigation, see
 * <a href="https://github.com/nextcloud/news-android/issues/1679">#1679</a>.
 */
@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "w740dp-h360dp-land-xxhdpi")
public class HorizontalInsetsTest {

    private static final int WINDOW_WIDTH = 2220;
    private static final int WINDOW_HEIGHT = 1080;
    private static final int NAVIGATION_BAR_INSET = 130;
    private static final int STATUS_BAR_INSET = 60;

    private View content;

    /**
     * Builds the given activity layout below a stand in for {@code android.R.id.content} and applies
     * the insets of a navigation bar on the left the way {@link PodcastFragmentActivity} does.
     */
    private void inflate(int activityLayout) {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        activity.setTheme(R.style.AppTheme);

        LayoutInflater inflater = LayoutInflater.from(activity);
        // the <fragment> tags are of no interest here, but need a stand in to be inflatable
        inflater.setFactory2(new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                return onCreateView(name, context, attrs);
            }

            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                return "fragment".equals(name) ? new FrameLayout(context) : null;
            }
        });

        content = new FrameLayout(activity);
        ((FrameLayout) content).addView(inflater.inflate(activityLayout, null));

        PodcastFragmentActivity.applyHorizontalInsets(content);
        ViewCompat.dispatchApplyWindowInsets(content, new WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(),
                        Insets.of(NAVIGATION_BAR_INSET, STATUS_BAR_INSET, 0, 0))
                .build());

        content.measure(
                View.MeasureSpec.makeMeasureSpec(WINDOW_WIDTH, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(WINDOW_HEIGHT, View.MeasureSpec.EXACTLY));
        content.layout(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /** Position of the given view within the window. */
    private int leftInWindow(View view) {
        int left = 0;
        for (View current = view; current != content; current = (View) current.getParent()) {
            left += current.getLeft();
        }
        return left;
    }

    private void assertToolbarClearsTheNavigationBar() {
        View toolbar = content.findViewById(R.id.toolbar);

        // the navigation menu button sits at the very start of the toolbar
        assertEquals(NAVIGATION_BAR_INSET, leftInWindow(toolbar));
        assertEquals(WINDOW_WIDTH - NAVIGATION_BAR_INSET, toolbar.getWidth());
    }

    @Test
    public void listViewToolbarClearsTheNavigationBar() {
        inflate(R.layout.activity_newsreader);

        assertToolbarClearsTheNavigationBar();
    }

    @Test
    public void detailViewToolbarClearsTheNavigationBar() {
        inflate(R.layout.activity_news_detail);

        assertToolbarClearsTheNavigationBar();
    }

    @Test
    public void slidingDrawerStartsBesideTheNavigationBar() {
        inflate(R.layout.activity_newsreader);

        assertEquals(NAVIGATION_BAR_INSET, leftInWindow(content.findViewById(R.id.drawer_layout)));
    }

    @Test
    public void verticalInsetsAreLeftToTheViewsHandlingThem() {
        inflate(R.layout.activity_newsreader);

        assertEquals(0, content.getPaddingTop());
        assertEquals(0, content.getPaddingBottom());
    }
}
