package de.luhmer.owncloudnewsreader.junit_tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import de.luhmer.owncloudnewsreader.BuildConfig;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TestDbTest {

    private NewsReaderListActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(NewsReaderListActivity.class).create().get();
    }

    @Test
    public void test() {
        assertTrue(true);
    }

    /*
    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }
    */
}
