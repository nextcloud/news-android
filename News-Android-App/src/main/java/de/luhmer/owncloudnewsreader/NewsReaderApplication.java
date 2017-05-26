package de.luhmer.owncloudnewsreader;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.AppComponent;
import de.luhmer.owncloudnewsreader.di.DaggerAppComponent;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.OkHttpImageDownloader;

public class NewsReaderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initDaggerAppComponent();


        //TODO check stuff below.. is this really required?
        HttpJsonRequest.init(this);
        initImageLoader();


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

    public void initImageLoader() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int diskCacheSize = Integer.parseInt(preferences.getString(SettingsActivity.SP_MAX_CACHE_SIZE,"500"))*1024*1024;
        if(ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().destroy();
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().
                cacheOnDisk(true).
                cacheInMemory(true).
                build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).
                diskCacheSize(diskCacheSize).
                memoryCacheSize(10 * 1024 * 1024).
                diskCacheFileNameGenerator(new Md5FileNameGenerator()).
                defaultDisplayImageOptions(imageOptions).
                imageDownloader(new OkHttpImageDownloader(this, HttpJsonRequest.getInstance().getImageClient())).
                build();
        ImageLoader.getInstance().init(config);
    }

    private AppComponent mAppComponent;

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
