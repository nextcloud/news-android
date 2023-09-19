package de.luhmer.owncloudnewsreader.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.test.InstrumentationRegistry;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.model.OcsUser;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;

public class TestApiModule extends ApiModule {

    private static final String TAG = TestApiModule.class.getCanonicalName();
    private final Application application;

    public static String DUMMY_ACCOUNT_AccountName = "test-account";
    public static String DUMMY_ACCOUNT_username = "david";
    public static String DUMMY_ACCOUNT_token = "abc";
    public static String DUMMY_ACCOUNT_server_url = "http://nextcloud.com/";

    public TestApiModule(Application application) {
        super(application);
        this.application = application;
    }

    @Override
    public SharedPreferences providesSharedPreferences() {
        // Create dummy account
        String prefKey = "PREF_ACCOUNT_STRING" + DUMMY_ACCOUNT_AccountName;
        SingleSignOnAccount ssoAccount = new SingleSignOnAccount(
                DUMMY_ACCOUNT_AccountName, DUMMY_ACCOUNT_username,
                DUMMY_ACCOUNT_token, DUMMY_ACCOUNT_server_url,
                "prod"
        );

        OcsUser userInfo = new OcsUser("1", DUMMY_ACCOUNT_username);

        //SharedPreferences sharedPrefs = new MockSharedPreference();
        SharedPreferences sharedPrefs = application.getSharedPreferences(providesSharedPreferencesFileName(), Context.MODE_PRIVATE);

        // Reset SharedPreferences to make tests reproducible
        sharedPrefs.edit().clear().commit();

        // Turn on Single-Sign-On
        sharedPrefs.edit().putBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, true).commit();

        // Set mock preferences for AccountImporter
        AccountImporter.setSharedPreferences(sharedPrefs);


        // Return mock login data when requesting the account
        try {
            sharedPrefs.edit().putString(prefKey, SingleSignOnAccount.toString(ssoAccount)).commit();
        } catch (IOException e) {
            throw new Error(e);
        }

        // For userinfo in main activity
        sharedPrefs.edit().putString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, DUMMY_ACCOUNT_server_url).commit();
        sharedPrefs.edit().putString(SettingsActivity.EDT_USERNAME_STRING, DUMMY_ACCOUNT_username).commit();
        sharedPrefs.edit().putString("PREF_CURRENT_ACCOUNT_STRING", DUMMY_ACCOUNT_AccountName).commit();
        try {
            sharedPrefs.edit().putString("USER_INFO", NewsReaderListFragment.toString(userInfo)).commit();
        } catch (IOException e) {
            throw new Error(e);
        }

        ThemeChooser.init(sharedPrefs);

        return sharedPrefs;
    }

   @Override
    public String providesSharedPreferencesFileName() {
        return application.getPackageName() + "_preferences_test";
    }

    @Override
    public String providesDatabaseFileName() {
        String filename = "OwncloudNewsReaderOrmTest.db";

        try {

            String dst = "/data/data/" + application.getApplicationContext().getPackageName() + "/databases/" + filename;
            File dstFile = new File(dst);
            dstFile.getParentFile().mkdirs();

            // https://stackoverflow.com/a/35690692
            copy(InstrumentationRegistry.getContext().getAssets().open("OwncloudNewsReaderOrm.db"), dstFile);
        } catch (IOException e) {
            Log.e(TAG, "Failed copying Test Database", e);
        }
        //return PreferenceManager.getDefaultSharedPreferencesName(mApplication);
        return filename;
    }

    @Override
    protected ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        ApiProvider apiProvider = new TestApiProvider(mtm, sp, application);
        return apiProvider;
    }



    public static void copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        in.close();

    }
}
