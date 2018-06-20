package de.luhmer.owncloudnewsreader.di;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.GsonConfig;
import de.luhmer.owncloudnewsreader.reader.OkHttpImageDownloader;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API_SSO;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by david on 26.05.17.
 */

public class ApiProvider {

    private static final String TAG = ApiProvider.class.getCanonicalName();
    private final MemorizingTrustManager mMemorizingTrustManager;
    private final SharedPreferences mPrefs;
    private API mApi;
    private Context context;


    public ApiProvider(MemorizingTrustManager mtm, SharedPreferences sp, Context context) {
        this.mMemorizingTrustManager = mtm;
        this.mPrefs = sp;
        this.context = context;
        initApi(new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onError(Exception ex) {

            }
        });
    }

    public void initApi(@NonNull NextcloudAPI.ApiConnectedListener apiConnectedListener) {
        String username   = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
        String password   = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
        String baseUrlStr = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "https://luhmer.de"); // We need to provide some sort of default URL here..
        Boolean useSSO    = mPrefs.getBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false);
        HttpUrl baseUrl = HttpUrl.parse(baseUrlStr).newBuilder()
                .addPathSegments("index.php/apps/news/api/v1-2/")
                .build();
        Log.d("ApiModule", "HttpUrl: " + baseUrl.toString());
        OkHttpClient client = OkHttpSSLClient.GetSslClient(baseUrl, username, password, mPrefs, mMemorizingTrustManager);
        initImageLoader(mPrefs, client, context);

        if(useSSO) {
            initSsoApi(apiConnectedListener);
        } else {
            initRetrofitApi(baseUrl, client);
            apiConnectedListener.onConnected();
        }
    }

    private void initRetrofitApi(HttpUrl baseUrl, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(GsonConfig.GetGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(client)
                .build();

        mApi = retrofit.create(API.class);
    }

    private void initSsoApi(final NextcloudAPI.ApiConnectedListener callback) {
        try {
            Account account = SingleAccountHelper.GetCurrentAccount(context);
            SingleSignOnAccount ssoAccount = AccountImporter.GetAuthTokenInSeparateThread(context, account);
            NextcloudAPI nextcloudAPI = new NextcloudAPI(ssoAccount, GsonConfig.GetGson());
            nextcloudAPI.start(context, callback);
            mApi = new API_SSO(nextcloudAPI);
        } catch (SSOException e) {
            callback.onError(e);
        }
    }



    private void initImageLoader(SharedPreferences mPrefs, OkHttpClient okHttpClient, Context context) {
        int diskCacheSize = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE,"500"))*1024*1024;
        if(ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCacheSize(diskCacheSize)
                .memoryCacheSize(10 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(imageOptions)
                .imageDownloader(new OkHttpImageDownloader(context, okHttpClient))
                .build();

        ImageLoader.getInstance().init(config);
    }

    public API getAPI() {
        return mApi;
    }
}
