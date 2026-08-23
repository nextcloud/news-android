package de.luhmer.owncloudnewsreader.junit_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ViewSwitcher;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.luhmer.owncloudnewsreader.R;

/**
 * Verifies that the podcast panel keeps clear of the system bars, see
 * <a href="https://github.com/nextcloud/news-android/issues/1647">#1647</a>. The panel spans the
 * whole window in {@code activity_newsreader}, while {@code activity_news_detail} positions it below
 * the toolbar - both are checked here.
 */
@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "w360dp-h740dp-xxhdpi")
public class PodcastPanelInsetsTest {

    private static final int WINDOW_WIDTH = 1080;
    private static final int WINDOW_HEIGHT = 2400;
    private static final int STATUS_BAR_INSET = 130;
    private static final int NAVIGATION_BAR_INSET = 60;

    private Activity activity;
    private View activityRoot;
    private SlidingUpPanelLayout slidingLayout;
    private View panel;
    private View panelContent;

    /**
     * Builds the given activity layout including the podcast fragment layout and applies the insets
     * the way {@code PodcastFragment} and {@code PodcastFragmentActivity} do at runtime.
     */
    private void inflate(int activityLayout, SlidingUpPanelLayout.PanelState panelState) {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
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

        activityRoot = inflater.inflate(activityLayout, null);
        slidingLayout = activityRoot.findViewById(R.id.sliding_layout);
        panel = activityRoot.findViewById(R.id.podcast_frame);

        panelContent = LayoutInflater.from(activity).inflate(R.layout.fragment_podcast, null);
        ((ViewGroup) panel).addView(panelContent);

        slidingLayout.setPanelState(panelState);

        // PodcastFragmentActivity lifts the collapsed mini player above the navigation bar
        slidingLayout.setPanelHeight(headerHeight() + NAVIGATION_BAR_INSET);
        // ... and PodcastFragment keeps the media controls of the expanded player clear of it
        panelContent.setPadding(0, 0, 0, NAVIGATION_BAR_INSET);
        // ... and grows the header by the inset of whichever screen edge it touches
        int slidingLayoutTop = ((ViewGroup.MarginLayoutParams) slidingLayout.getLayoutParams()).topMargin;
        boolean expanded = panelState == SlidingUpPanelLayout.PanelState.EXPANDED;
        // the header swaps its content along with the panel state
        ((ViewSwitcher) header()).setDisplayedChild(expanded ? 1 : 0);
        int headerTop = expanded ? Math.max(0, STATUS_BAR_INSET - slidingLayoutTop) : 0;
        int headerBottom = expanded ? 0 : NAVIGATION_BAR_INSET;
        header().setPadding(0, headerTop, 0, headerBottom);
        header().getLayoutParams().height = headerHeight() + headerTop + headerBottom;

        activityRoot.measure(
                View.MeasureSpec.makeMeasureSpec(WINDOW_WIDTH, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(WINDOW_HEIGHT, View.MeasureSpec.EXACTLY));
        activityRoot.layout(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private int headerHeight() {
        return activity.getResources().getDimensionPixelSize(R.dimen.podcast_header_height);
    }

    /** Position of the given view within the window. */
    private int topInWindow(View view) {
        int top = 0;
        for (View current = view; current != activityRoot; current = (View) current.getParent()) {
            top += current.getTop();
        }
        return top;
    }

    private View header() {
        return panelContent.findViewById(R.id.viewSwitcherProgress);
    }

    private View playbackButton() {
        return panelContent.findViewById(R.id.btn_playPausePodcastSlider);
    }

    /** Wrapper of the mini player play button, which spans the whole content of the header. */
    private View miniPlayerButton() {
        return panelContent.findViewById(R.id.fl_playPausePodcastWrapper);
    }

    private View expandedTitle() {
        return panelContent.findViewById(R.id.tv_titleSlider);
    }

    private void assertPlaybackControlsAboveNavigationBar() {
        assertTrue("playback controls must stay above the navigation bar",
                topInWindow(playbackButton()) + playbackButton().getHeight()
                        <= WINDOW_HEIGHT - NAVIGATION_BAR_INSET);
    }

    @Test
    public void collapsedMiniPlayerSitsAboveTheNavigationBar() {
        inflate(R.layout.activity_newsreader, SlidingUpPanelLayout.PanelState.COLLAPSED);

        assertEquals(headerHeight(), miniPlayerButton().getHeight());
        assertEquals(WINDOW_HEIGHT - NAVIGATION_BAR_INSET,
                topInWindow(miniPlayerButton()) + miniPlayerButton().getHeight());
    }

    @Test
    public void collapsedHeaderFillsTheNavigationBarArea() {
        inflate(R.layout.activity_newsreader, SlidingUpPanelLayout.PanelState.COLLAPSED);

        // nothing but the header may show up behind the navigation bar
        assertEquals(WINDOW_HEIGHT, topInWindow(header()) + header().getHeight());
    }

    @Test
    public void expandedPlayerSitsBetweenTheSystemBars() {
        inflate(R.layout.activity_newsreader, SlidingUpPanelLayout.PanelState.EXPANDED);

        // the panel covers the status bar, the header content starts below it
        assertEquals(0, topInWindow(header()));
        assertEquals(STATUS_BAR_INSET, topInWindow(expandedTitle()));
        assertEquals(headerHeight(), header().getHeight() - STATUS_BAR_INSET);
        assertPlaybackControlsAboveNavigationBar();
    }

    @Test
    public void detailViewCollapsedMiniPlayerIsUnaffected() {
        inflate(R.layout.activity_news_detail, SlidingUpPanelLayout.PanelState.COLLAPSED);

        assertEquals(WINDOW_HEIGHT - NAVIGATION_BAR_INSET,
                topInWindow(miniPlayerButton()) + miniPlayerButton().getHeight());
        assertEquals(WINDOW_HEIGHT, topInWindow(header()) + header().getHeight());
    }

    @Test
    public void detailViewExpandedPlayerIsUnchanged() {
        inflate(R.layout.activity_news_detail, SlidingUpPanelLayout.PanelState.EXPANDED);

        // the panel already starts below the status bar, so it must not be pushed down again
        assertTrue("panel must not reach into the status bar",
                topInWindow(slidingLayout) >= STATUS_BAR_INSET);
        assertEquals(topInWindow(slidingLayout), topInWindow(expandedTitle()));
        assertPlaybackControlsAboveNavigationBar();
    }
}
