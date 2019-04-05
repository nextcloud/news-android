package de.luhmer.owncloudnewsreader.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestApiModule extends ApiModule {

    private Application application;

    public TestApiModule(Application application) {
        super(application);
        this.application = application;
    }

    @Override
    public SharedPreferences providesSharedPreferences() {
        SharedPreferences sharedPrefs = mock(SharedPreferences.class);
        final Context context = mock(Context.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);

        // Turn on Single-Sign-On
        when(sharedPrefs.getBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false)).thenReturn(true);

        // Set cache size
        when(sharedPrefs.getString(eq(SettingsActivity.SP_MAX_CACHE_SIZE), any())).thenReturn("500");


        // Add dummy account
        String accountName = "test-account";
        String username = "david";
        String token = "abc";
        String server_url = "http://nextcloud.com/";

        String prefKey = "PREF_ACCOUNT_STRING" + accountName;
        SingleSignOnAccount ssoAccount = new SingleSignOnAccount(accountName, username, token, server_url);

        try {
            AccountImporter.getSharedPreferences(application).edit().putString(prefKey, SingleSignOnAccount.toString(ssoAccount)).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //try {
        //    when(sharedPrefs.getString(eq(prefKey), any())).thenReturn(SingleSignOnAccount.toString(ssoAccount));
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        SingleAccountHelper.setCurrentAccount(application, accountName);

        return sharedPrefs;
    }

    private final String NEW_FEED_SUCCESS  = "http://test.de/new";
    private final String NEW_FEED_EXISTING = "http://test.de/existing";
    private final String NEW_FEED_FAIL     = "http://test.de/fail";
    private final String NEW_FEED_EXISTING_ERROR_MESSAGE = "{\"message\":\"Feed konnte nicht hinzugef\\u00fcgt werden:  Existiert bereits\"}";
    private final String NEW_FEED_FAIL_ERROR_MESSAGE     = "{\"message\":\"FeedIo\\\\Adapter\\\\NotFoundException: Client error: `GET http:\\/\\/feeds2.feedburner.com\\/stadt-bremerhaven\\/dqXM222` resulted in a `404 Feed not found error: FeedBurner cannot locate this feed URI.` response:\\n\\u003Chtml\\u003E\\n\\u003Chead\\u003E\\n\\u003Cstyle type=\\\"text\\/css\\\"\\u003E\\na:link, a:visited {\\n  color: #000099;\\n  text-decoration: underline;\\n}\\n\\na:hover {\\n  (truncated...)\\n in \\/apps2\\/news\\/lib\\/Fetcher\\/Client\\/FeedIoClient.php:57\\nStack trace:\\n#0 \\/apps2\\/news\\/vendor\\/debril\\/feed-io\\/src\\/FeedIo\\/Reader.php(116): OCA\\\\News\\\\Fetcher\\\\Client\\\\FeedIoClient-\\u003EgetResponse('http:\\/\\/feeds2.f...', Object(DateTime))\\n#1 \\/apps2\\/news\\/vendor\\/debril\\/feed-io\\/src\\/FeedIo\\/FeedIo.php(286): FeedIo\\\\Reader-\\u003Eread('http:\\/\\/feeds2.f...', Object(FeedIo\\\\Feed), Object(DateTime))\\n#2 \\/apps2\\/news\\/lib\\/Fetcher\\/FeedFetcher.php(77): FeedIo\\\\FeedIo-\\u003Eread('http:\\/\\/feeds2.f...')\\n#3 \\/apps2\\/news\\/lib\\/Fetcher\\/Fetcher.php(68): OCA\\\\News\\\\Fetcher\\\\FeedFetcher-\\u003Efetch('http:\\/\\/feeds2.f...', true, NULL, NULL, NULL)\\n#4 \\/apps2\\/news\\/lib\\/Service\\/FeedService.php(116): OCA\\\\News\\\\Fetcher\\\\Fetcher-\\u003Efetch('http:\\/\\/feeds2.f...', true, NULL, NULL, NULL)\\n#5 \\/apps2\\/news\\/lib\\/Controller\\/FeedApiController.php(96): OCA\\\\News\\\\Service\\\\FeedService-\\u003Ecreate('http:\\/\\/feeds2.f...', 0, 'david')\\n#6 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Http\\/Dispatcher.php(166): OCA\\\\News\\\\Controller\\\\FeedApiController-\\u003Ecreate('http:\\/\\/feeds2.f...', 0)\\n#7 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Http\\/Dispatcher.php(99): OC\\\\AppFramework\\\\Http\\\\Dispatcher-\\u003EexecuteController(Object(OCA\\\\News\\\\Controller\\\\FeedApiController), 'create')\\n#8 \\/nextcloud\\/lib\\/private\\/AppFramework\\/App.php(118): OC\\\\AppFramework\\\\Http\\\\Dispatcher-\\u003Edispatch(Object(OCA\\\\News\\\\Controller\\\\FeedApiController), 'create')\\n#9 \\/nextcloud\\/lib\\/private\\/AppFramework\\/Routing\\/RouteActionHandler.php(47): OC\\\\AppFramework\\\\App::main('OCA\\\\\\\\News\\\\\\\\Contro...', 'create', Object(OC\\\\AppFramework\\\\DependencyInjection\\\\DIContainer), Array)\\n#10 [internal function]: OC\\\\AppFramework\\\\Routing\\\\RouteActionHandler-\\u003E__invoke(Array)\\n#11 \\/nextcloud\\/lib\\/private\\/Route\\/Router.php(297): call_user_func(Object(OC\\\\AppFramework\\\\Routing\\\\RouteActionHandler), Array)\\n#12 \\/nextcloud\\/lib\\/base.php(987): OC\\\\Route\\\\Router-\\u003Ematch('\\/apps\\/news\\/api\\/...')\\n#13 \\/nextcloud\\/index.php(42): OC::handleRequest()\\n#14 {main}\"}";


    @Override
    ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        API api = Mockito.mock(API.class);
        when(apiProvider.getAPI()).thenReturn(api);

        Call<List<Feed>> mockCallSuccess  = mockCreateFeed(NEW_FEED_SUCCESS);
        Call<List<Feed>> mockCallExisting = mockCreateFeed(NEW_FEED_EXISTING);
        Call<List<Feed>> mockCallFail     = mockCreateFeed(NEW_FEED_FAIL);

        when(api.createFeed(eq(NEW_FEED_SUCCESS),  any())).thenReturn(mockCallSuccess);
        when(api.createFeed(eq(NEW_FEED_EXISTING), any())).thenReturn(mockCallExisting);
        when(api.createFeed(eq(NEW_FEED_FAIL),     any())).thenReturn(mockCallFail);

        return apiProvider;
    }

    // https://github.com/nextcloud/news/blob/master/docs/externalapi/Legacy.md#create-a-feed
    private Call<List<Feed>> mockCreateFeed(String url) {
        Call<List<Feed>> mockCall = Mockito.mock(Call.class);

        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            Callback callback = (Callback) args[0];
            final Thread thr = new Thread() {
                @Override
                public void run() {
                    switch(url) {
                        case NEW_FEED_SUCCESS:
                            callback.onResponse(mockCall, Response.success(""));
                            break;
                        case NEW_FEED_EXISTING:
                            callback.onResponse(mockCall, Response.error(409, ResponseBody.create(null, NEW_FEED_EXISTING_ERROR_MESSAGE)));
                            break;
                        case NEW_FEED_FAIL:
                            callback.onResponse(mockCall,Response.error(422 , ResponseBody.create(null, NEW_FEED_FAIL_ERROR_MESSAGE)));
                            break;
                        default:
                            throw new RuntimeException("URL NOT KNOWN FOR TEST!");
                    }
                }
            };
            thr.start();
            return null;
        }).when(mockCall).enqueue(any());

        return mockCall;
    }

}
