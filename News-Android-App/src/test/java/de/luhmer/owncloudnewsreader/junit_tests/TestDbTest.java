package de.luhmer.owncloudnewsreader.junit_tests;

import android.database.Cursor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.List;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.DatabaseHelperOrm;
import de.luhmer.owncloudnewsreader.database.model.DaoSession;
import de.luhmer.owncloudnewsreader.database.model.Feed;

import static org.junit.Assert.*;


@Config(emulateSdk=18)
@RunWith(RobolectricGradleTestRunner.class)
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
