package de.luhmer.owncloudnewsreader.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;


import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import dagger.Provides;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestApiModule extends ApiModule {

    public TestApiModule(Application application) {
        super(application);
    }

    @Override
    public SharedPreferences providesSharedPreferences() {
        SharedPreferences sharedPrefs = Mockito.mock(SharedPreferences.class);
        final Context context = Mockito.mock(Context.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        return sharedPrefs;
    }

    @Override
    ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        API api = Mockito.mock(API.class);
        when(apiProvider.getAPI()).thenReturn(api);

        apiProvider.setAPI(api);

        Call mockCall = Mockito.mock(Call.class);
        when(api.createFeed(any())).thenReturn(mockCall);


        /*
        doAnswer(invocation -> {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                throw new NetworkOnMainThreadException();
            }
            return null;
        }).when(mockCall).enqueue(any());
        */



        return apiProvider;
    }

}
