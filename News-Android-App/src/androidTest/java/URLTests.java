import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import okhttp3.HttpUrl;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class URLTests extends ActivityInstrumentationTestCase2<NewsReaderListActivity> {

    private class TestAPI extends API {

        public TestAPI(HttpUrl baseUrl) {
            super(baseUrl);
        }

        @Override
        public HttpUrl getItemUrl() {
            return null;
        }

        @Override
        public HttpUrl getItemUpdatedUrl() {
            return null;
        }

        @Override
        public HttpUrl getFeedUrl() {
            return null;
        }

        @Override
        public HttpUrl getFolderUrl() {
            return null;
        }

        @Override
        public HttpUrl getUserUrl() {
            return null;
        }

        @Override
        public HttpUrl getTagBaseUrl() {
            return null;
        }

        @Override
        public boolean PerformTagExecution(List<String> itemIds, FeedItemTags tag, Context context) {
            return false;
        }

        public HttpUrl getApiUrl(String format, String... urlSegments) {
            return getAPIUrl(format, urlSegments);
        }
    }
    private NewsReaderListActivity mActivity;

    public URLTests() {
        super(NewsReaderListActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();

        HttpJsonRequest.init(mActivity);
    }

    @Test
    public void testUrl() {
        // Key: Root Url, Value: Expected result
        Map<String, HttpUrl> testUrls = new HashMap<>();

        testUrls.put("https://test.com", HttpUrl.parse("https://test.com/test1/test2/test3"));
        testUrls.put("https://test.com/", HttpUrl.parse("https://test.com/test1/test2/test3"));

        testUrls.put("https://test.com/subfolder", HttpUrl.parse("https://test.com/subfolder/test1/test2/test3"));
        testUrls.put("https://test.com/subfolder/", HttpUrl.parse("https://test.com/subfolder/test1/test2/test3"));

        for(Map.Entry<String, HttpUrl> testUrlEntry: testUrls.entrySet()) {
            HttpJsonRequest.getInstance().setCredentials("test", "test", testUrlEntry.getKey());
            TestAPI api = new TestAPI(HttpJsonRequest.getInstance().getRootUrl());
            HttpUrl apiUrl = api.getApiUrl(null, "./test1/test2", "test3");
            assertTrue(apiUrl.equals(testUrlEntry.getValue()));
        }
    }
}