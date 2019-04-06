package de.luhmer.owncloudnewsreader.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

    @Override
    protected ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        ApiProvider apiProvider = new TestApiProvider(mtm, sp, application);
        return apiProvider;
    }
}
