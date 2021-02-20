package de.luhmer.owncloudnewsreader;

import android.os.Build;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.AppComponent;
import de.luhmer.owncloudnewsreader.di.DaggerAppComponent;
import de.luhmer.owncloudnewsreader.helper.AdBlocker;
import de.luhmer.owncloudnewsreader.helper.ForegroundListener;

public class NewsReaderApplication extends MultiDexApplication {

    protected AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        setupStrictMode();

        registerActivityLifecycleCallbacks(new ForegroundListener());

        initDaggerAppComponent();

        AdBlocker.init(this);
    }

    private void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                    .permitDiskReads(); // Too many violations
            threadPolicyBuilder.penaltyDeath();
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectFileUriExposure()
                    .penaltyLog();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.detectContentUriWithoutPermission();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.detectNonSdkApiUsage();
            }

            builder.penaltyDeath();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    public void initDaggerAppComponent() {
        // Dagger%COMPONENT_NAME%

        mAppComponent = DaggerAppComponent.builder()
                .apiModule(new ApiModule(this))
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //mAppComponent = DaggerAppComponent.create();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
