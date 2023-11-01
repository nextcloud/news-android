package de.luhmer.owncloudnewsreader.di;

import static de.luhmer.owncloudnewsreader.helper.NextcloudGlideModuleKt.CACHE_SIZE;
import static de.luhmer.owncloudnewsreader.helper.NextcloudGlideModuleKt.MB;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by david on 22.05.17.
 */

@Module
public class ApiModule {

    private final Application mApplication;

    public ApiModule(Application application) {
        this.mApplication = application;
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Singleton
    // Application reference must come from AppModule.class
    SharedPreferences providesSharedPreferences() {
        //return PreferenceManager.getDefaultSharedPreferences(mApplication);
        SharedPreferences mPrefs = mApplication.getSharedPreferences(providesSharedPreferencesFileName(), Context.MODE_PRIVATE);
        ThemeChooser.init(mPrefs);
        return mPrefs;
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Named("sharedPreferencesFileName")
    public String providesSharedPreferencesFileName() {
        //return PreferenceManager.getDefaultSharedPreferencesName(mApplication);
        return mApplication.getPackageName() + "_preferences";
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Named("databaseFileName")
    public String providesDatabaseFileName() {
        //return PreferenceManager.getDefaultSharedPreferencesName(mApplication);
        return "OwncloudNewsReaderOrm.db";
    }

    /*
    @Provides
    @Singleton
    NextcloudAPI providexNextcloudAPI() {
        return new NextcloudAPI("");
    }*/

    /*
    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }*/

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    PostDelayHandler providePostDelayHandler() {
        return new PostDelayHandler(mApplication);
    }


    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(SharedPreferences prefs) {
        String cacheSize = prefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE, String.valueOf(CACHE_SIZE));
        Long diskCacheSizeBytes = Long.valueOf(cacheSize) * MB;

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .cache(new Cache(mApplication.getApplicationContext().getCacheDir(), diskCacheSizeBytes));
        /*
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val response = chain.proceed(chain.request())
            if (response.cacheResponse != null) {
                Log.d("NextcloudGlideModule", "cached response: " + response.request.url)
            } else if (response.networkResponse != null) {
                Log.d("NextcloudGlideModule", "network response: " + response.request.url)
                for (h in response.request.headers) {
                    Log.d("NextcloudGlideModule", "request headers: $h")
                }
            }
            response
        })
         */
        return client.build();
    }

    /*
    @Provides
    @Singleton
    Retrofit provideRetrofit(String baseUrl, Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
        return retrofit;
    }
    */

    @Provides
    @Singleton
    MemorizingTrustManager provideMTM() {
        return new MemorizingTrustManager(mApplication);
    }

    @Provides
    @Singleton
    ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        return new ApiProvider(mtm, sp, mApplication);
    }

}