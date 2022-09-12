package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;
import com.github.technoir42.glide.debug.indicator.DebugIndicatorTransitionFactory;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.di.ApiProvider;

@GlideModule
public class NextcloudGlideModule extends AppGlideModule {
    private String TAG = NextcloudGlideModule.class.getCanonicalName();

    protected @Inject ApiProvider mApi;
    protected @Inject SharedPreferences mPrefs;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // #00ff00 Memory Cache (Green)
        // #0066ff Disk Cache (Blue)
        // #ff0000 Remote (Red)
        // #ffff00 Local (Yellow)
        builder.setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.with(DebugIndicatorTransitionFactory.DEFAULT));

        ((NewsReaderApplication) context.getApplicationContext()).getAppComponent().injectGlideModule(this);

        String cacheSize = mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE,"500");
        int diskCacheSizeBytes = Integer.parseInt(cacheSize)*1024*1024;

        // Glide uses DiskLruCacheWrapper as the default DiskCache. DiskLruCacheWrapper is a fixed
        // size disk cache with LRU eviction. The default disk cache size is 250 MB and is placed
        // in a specific directory in the Application’s cache folder.

        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        // builder.setDiskCache(new ExternalCacheDiskCacheFactory(context));
    }
}