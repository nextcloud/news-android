package de.luhmer.owncloudnewsreader.helper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.samples.svg.SvgDecoder
import com.bumptech.glide.samples.svg.SvgDrawableTranscoder
import com.caverock.androidsvg.SVG
import de.luhmer.owncloudnewsreader.NewsReaderApplication
import de.luhmer.owncloudnewsreader.SettingsActivity
import de.luhmer.owncloudnewsreader.di.ApiProvider
import java.io.InputStream
import javax.inject.Inject

private const val CACHE_SIZE = 500

private const val KB = 1024
private const val MB = 1024 * KB

@GlideModule
class NextcloudGlideModule : AppGlideModule() {
    @Inject
    lateinit var mApi: ApiProvider

    @Inject
    lateinit var mPrefs: SharedPreferences

    override fun applyOptions(
        context: Context,
        builder: GlideBuilder,
    ) {
        super.applyOptions(context, builder)
        (context.applicationContext as NewsReaderApplication).appComponent.injectGlideModule(this)
        val cacheSize = mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE, CACHE_SIZE.toString())
        val diskCacheSizeBytes = (cacheSize?.toInt() ?: CACHE_SIZE) * MB

        // Glide uses DiskLruCacheWrapper as the default DiskCache. DiskLruCacheWrapper is a fixed
        // size disk cache with LRU eviction. The default disk cache size is 250 MB and is placed
        // in a specific directory in the Application’s cache folder.
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        // builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context))

        // #00ff00 Memory Cache (Green)
        // #0066ff Disk Cache (Blue)
        // #ff0000 Remote (Red)
        // #ffff00 Local (Yellow)

        // enable caching indicators for Glide
        // builder.setDefaultTransitionOptions(
        //         Drawable::class.java,
        //         DrawableTransitionOptions.with(DebugIndicatorTransitionFactory.DEFAULT)
        // )
    }

    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry,
    ) {
        super.registerComponents(context, glide, registry)
        registry
            .register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }
}
