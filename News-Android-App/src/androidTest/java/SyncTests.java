import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudReaderMethods;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SyncTests extends ActivityInstrumentationTestCase2<NewsReaderListActivity> {

    private NewsReaderListActivity mActivity;
    private MockWebServer server;
    private HttpUrl baseUrl;

    public SyncTests() {
        super(NewsReaderListActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();

        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        server = new MockWebServer();
        server.start();
        // Ask the server for its URL. You'll need this to make HTTP requests.
        baseUrl = server.url("/");
        HttpJsonRequest.init(mActivity);
        HttpJsonRequest.getInstance().setCredentials("test", "test", baseUrl.toString());
    }

    @Test
    public void testVersionInfo() throws Exception {
        // Schedule some responses.
        server.enqueue(new MockResponse().setBody(getSampleVersionInfoV2()));

        String versionNumber = OwnCloudReaderMethods.GetVersionNumber(baseUrl);
        assertEquals("5.2.3", versionNumber);

        API api = API.GetRightApiForVersion(versionNumber, baseUrl);
        assertTrue(api instanceof APIv2);
    }

    private String getSampleVersionInfoV2() {
        JsonObject jVer = new JsonObject();
        jVer.addProperty("version", "5.2.3");
        return jVer.toString();
    }


    @Test
    public void testFeedSync() throws Exception {

        JsonObject jFeed = new JsonObject();
        JsonObject jF = new JsonObject();
        jF.addProperty("id", "-1");
        jF.addProperty("url", "");
        jF.addProperty("title", "-1");
        jF.addProperty("faviconLink", "-1");
        jF.addProperty("added", "-1");
        jF.addProperty("folderId", "-1");
        jF.addProperty("ordering", "-1"); //TODO implement this field!
        jF.addProperty("link", "-1");
        jF.addProperty("pinned", "-1");
        JsonArray jFeedArr = new JsonArray();
        jFeedArr.add(jF);
        jFeed.add("feeds", jFeedArr);


        server.enqueue(new MockResponse().setBody(getSampleVersionInfoV2()));
        server.enqueue(new MockResponse().setBody(jFeed.toString()));

        String versionNumber = OwnCloudReaderMethods.GetVersionNumber(baseUrl);
        API api = API.GetRightApiForVersion(versionNumber, baseUrl);
        assertTrue(api instanceof APIv2);

        int[] res = OwnCloudReaderMethods.GetFeeds(mActivity, api);
        assertEquals(1, res[0]);
        assertEquals(1, res[1]);
    }


    @Test
    public void testItemSync() throws Exception {
        JsonObject jItem = new JsonObject();
        JsonObject jI = new JsonObject();
        jI.addProperty("id", "-1");
        jI.addProperty("guid", "http://grulja.wordpress.com/?p=76");
        jI.addProperty("guidHash", "3059047a572cd9cd5d0bf645faffd077");
        jI.addProperty("url", "http://grulja.wordpress.com/2013/04/29/plasma-nm-after-the-solid-sprint/");
        jI.addProperty("title", "Plasma-nm after the solid sprint");
        jI.addProperty("author", "Jan Grulich (grulja)");
        jI.addProperty("pubDate", 1367270544);
        jI.addProperty("body", "<p>At first I have to say...</p>");
        jI.addProperty("enclosureMime", (String) null);
        jI.addProperty("enclosureLink", (String) null);
        jI.addProperty("feedId", "-1");
        jI.addProperty("unread", true);
        jI.addProperty("starred", false);
        jI.addProperty("lastModified", 1367273003);

        JsonArray jItemArr = new JsonArray();
        jItemArr.add(jI);
        jItem.add("feeds", jItemArr);


        server.enqueue(new MockResponse().setBody(getSampleVersionInfoV2()));
        server.enqueue(new MockResponse().setBody(jItem.toString()));

        String versionNumber = OwnCloudReaderMethods.GetVersionNumber(baseUrl);
        API api = API.GetRightApiForVersion(versionNumber, baseUrl);
        assertTrue(api instanceof APIv2);

        int res2 = OwnCloudReaderMethods.GetItems(FeedItemTags.ALL, mActivity, "0", true, "0", "0", api); //TODO verify params
        assertEquals(1, res2);


        /*
        // Optional: confirm that your app made the HTTP requests you were expecting.
        RecordedRequest request1 = server.takeRequest();
        assertEquals("/v1/chat/messages/", request1.getPath());
        assertNotNull(request1.getHeader("Authorization"));

        RecordedRequest request2 = server.takeRequest();
        assertEquals("/v1/chat/messages/2", request2.getPath());

        RecordedRequest request3 = server.takeRequest();
        assertEquals("/v1/chat/messages/3", request3.getPath());
        */
    }

    @After
    public void tearDown() throws Exception {
        // Shut down the server. Instances cannot be reused.
        server.shutdown();

        super.tearDown();
    }


    private void sleep(float seconds) {
        try {
            Thread.sleep((long) seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}