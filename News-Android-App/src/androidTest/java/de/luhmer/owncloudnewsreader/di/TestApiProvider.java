package de.luhmer.owncloudnewsreader.di;

import static org.mockito.ArgumentMatchers.any;
import static de.luhmer.owncloudnewsreader.di.TestApiModule.DUMMY_ACCOUNT_AccountName;
import static de.luhmer.owncloudnewsreader.di.TestApiModule.DUMMY_ACCOUNT_username;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NetworkRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.Response;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.luhmer.owncloudnewsreader.helper.GsonConfig;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NewsAPI;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import retrofit2.NextcloudRetrofitApiBuilder;

public class TestApiProvider extends ApiProvider {

    private static final String TAG = TestApiProvider.class.getCanonicalName();

    public static final String NEW_FEED_SUCCESS  = "http://test.de/new";
    public static final String NEW_FEED_EXISTING = "http://test.de/existing";
    public static final String NEW_FEED_FAIL     = "http://test.de/fail";
    private static final String NEW_FEED_EXISTING_ERROR_MESSAGE = "{\"message\":\"Feed konnte nicht hinzugef\\u00fcgt werden:  Existiert bereits\"}";
    private static final String NEW_FEED_FAIL_ERROR_MESSAGE     = "{\"message\":\"FeedIo\\\\Adapter\\\\NotFoundException: Client error: `GET http:\\/\\/feeds2.feedburner.com\\/stadt-bremerhaven\\/dqXM222` resulted in a `404 Feed not found error: FeedBurner cannot locate this feed URI.` response:\\n\\u003Chtml\\u003E\\n\\u003Chead\\u003E\\n\\u003Cstyle type=\\\"text\\/css\\\"\\u003E\\na:link, a:visited {\\n  color: #000099;\\n  text-decoration: underline;\\n}\\n\\na:hover {\\n  (truncated...)\\n in \\/apps2\\/news\\/lib\\/Fetcher\\/Client\\/FeedIoClient.php:57\\nStack trace:\\n#0 \\/apps2\\/news\\/vendor\\/debril\\/feed-io\\/src\\/FeedIo\\/Reader.php(116): OCA\\\\News\\\\Fetcher\\\\Client\\\\FeedIoClient-\\u003EgetResponse('http:\\/\\/feeds2.f...', Object(DateTime))\\n#1 \\/apps2\\/news\\/vendor\\/debril\\/feed-io\\/src\\/FeedIo\\/FeedIo.php(286): FeedIo\\\\Reader-\\u003Eread('http:\\/\\/feeds2.f...', Object(FeedIo\\\\Feed), Object(DateTime))\\n#2 \\/apps2\\/news\\/lib\\/Fetcher\\/FeedFetcher.php(77): FeedIo\\\\FeedIo-\\u003Eread('http:\\/\\/feeds2.f...')\\n#3 \\/apps2\\/news\\/lib\\/Fetcher\\/Fetcher.php(68): OCA\\\\News\\\\Fetcher\\\\FeedFetcher-\\u003Efetch('http:\\/\\/feeds2.f...', true, NULL, NULL, NULL)\\n#4 \\/apps2\\/news\\/lib\\/Service\\/FeedService.php(116): OCA\\\\News\\\\Fetcher\\\\Fetcher-\\u003Efetch('http:\\/\\/feeds2.f...', true, NULL, NULL, NULL)\\n#5 \\/apps2\\/news\\/lib\\/Controller\\/FeedApiController.php(96): OCA\\\\News\\\\Service\\\\FeedService-\\u003Ecreate('http:\\/\\/feeds2.f...', 0, 'david')\\n#6 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Http\\/Dispatcher.php(166): OCA\\\\News\\\\Controller\\\\FeedApiController-\\u003Ecreate('http:\\/\\/feeds2.f...', 0)\\n#7 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Http\\/Dispatcher.php(99): OC\\\\AppFramework\\\\Http\\\\Dispatcher-\\u003EexecuteController(Object(OCA\\\\News\\\\Controller\\\\FeedApiController), 'create')\\n#8 \\/nextcloud\\/lib\\/private\\/AppFramework\\/App.php(118): OC\\\\AppFramework\\\\Http\\\\Dispatcher-\\u003Edispatch(Object(OCA\\\\News\\\\Controller\\\\FeedApiController), 'create')\\n#9 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Routing\\/RouteActionHandler.php(47): OC\\\\AppFramework\\\\App::main('OCA\\\\\\\\News\\\\\\\\Contro...', 'create', Object(OC\\\\AppFramework\\\\DependencyInjection\\\\DIContainer), Array)\\n#10 [internal function]: OC\\\\AppFramework\\\\Routing\\\\RouteActionHandler-\\u003E__invoke(Array)\\n#11 \\/nextcloud\\/lib\\/private\\/Route\\/Router.php(297): call_user_func(Object(OC\\\\AppFramework\\\\Routing\\\\RouteActionHandler), Array)\\n#12 \\/nextcloud\\/lib\\/base.php(987): OC\\\\Route\\\\Router-\\u003Ematch('\\/apps\\/news\\/api\\/...')\\n#13 \\/nextcloud\\/index.php(42): OC::handleRequest()\\n#14 {main}\"}";


    public NewsTestNetworkRequest networkRequestSpy;

    TestApiProvider(MemorizingTrustManager mtm, SharedPreferences sp, Context context) {
        super(mtm, sp, context);
    }

    @Override
    protected void initSsoApi(final NextcloudAPI.ApiConnectedListener callback) {
        NewsTestNetworkRequest networkRequest = new NewsTestNetworkRequest(context, callback);
        networkRequestSpy = Mockito.spy(networkRequest);

        // By spying on the method "performNetworkRequest" we can later check if requests were build correctly
        try {
            Mockito.doCallRealMethod().when(networkRequestSpy).performNetworkRequest(any(), any());
        } catch (Exception e) {
            e.printStackTrace();
        }

        NextcloudAPI nextcloudAPI = new NextcloudAPI(GsonConfig.GetGson(), networkRequestSpy);
        mNewsApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, NewsAPI.mApiEndpoint).create(NewsAPI.class);
    }


    public class NewsTestNetworkRequest extends NetworkRequest {

        NewsTestNetworkRequest(Context context, NextcloudAPI.ApiConnectedListener callback) {
            super(null, null, callback);
        }

        @Override
        protected void connect(String type) {
            super.connect(type);
            mCallback.onConnected();
        }

        public InputStream performNetworkRequest(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                throw new NetworkOnMainThreadException();
            }

            Log.w(TAG, "Requested URL: " + request.getUrl());
            InputStream inputStream;
            switch (request.getUrl()) {
                case "/index.php/apps/news/api/v1-2/feeds":
                    if("POST".equals(request.getMethod())) {
                        inputStream = handleCreateFeed(request);
                    } else {
                        inputStream = stringToInputStream("{\"feeds\": []}");
                    }
                    break;
                case "/index.php/apps/news/api/v1-2/user":
                    inputStream = handleUser();
                    break;
                case "/index.php/apps/news/api/v1-2/folders":
                    inputStream = handleFolders();
                    break;
                case "/index.php/apps/news/api/v1-2/items":
                    inputStream = stringToInputStream("{\"items\": []}");
                    break;
                //case "index.php/apps/news/api/v1-2/feeds":
                case "/index.php/apps/news/api/v1-2/items/unread/multiple":
                    inputStream = stringToInputStream("");
                    break;
                default:
                    Log.e(TAG, request.getUrl());
                    throw new Error("Not implemented yet!");
            }
            return inputStream;
        }

        @Override
        protected Response performNetworkRequestV2(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception {
            return new Response(
                    performNetworkRequest(request, requestBodyInputStream), new ArrayList<>(0)
            );
        }

        private InputStream handleFolders() {
            String folders = "{\"folders\":[{\"id\":2,\"name\":\"Comic\"},{\"id\":3,\"name\":\"Android\"}]}";
            return stringToInputStream(folders);
        }


        // https://github.com/nextcloud/news/blob/master/docs/externalapi/Legacy.md#create-a-feed
        private InputStream handleCreateFeed(NextcloudRequest request) throws NextcloudHttpRequestFailedException {
            var url = request.getParameterV2().stream().filter((s) -> s.key.equals("url")).findFirst().get().value;
            switch (url) {
                case NEW_FEED_SUCCESS:
                    return stringToInputStream("");
                case NEW_FEED_EXISTING:
                    throw new NextcloudHttpRequestFailedException(context, 409, new Throwable(NEW_FEED_EXISTING_ERROR_MESSAGE));
                case NEW_FEED_FAIL:
                    throw new NextcloudHttpRequestFailedException(context, 422, new Throwable(NEW_FEED_FAIL_ERROR_MESSAGE));
                default:
                    throw new Error("Not implemented yet!");
            }
        }

        private InputStream handleUser() {
            String user = "{\n" +
            "  \"userId\": \"" + DUMMY_ACCOUNT_AccountName + "\",\n" +
            "  \"displayName\": \"" + DUMMY_ACCOUNT_username + "\",\n" +
            "  \"lastLoginTimestamp\": 1241231233, \n" +
            "  \"avatar\": null" +
            "}";

            return stringToInputStream(user);
        }

        private InputStream stringToInputStream(String data) {
            return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        }
    }
}
