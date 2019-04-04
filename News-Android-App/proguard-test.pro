# proguard-test.pro:
-include proguard-rules.pro
-keepattributes SourceFile,LineNumberTable



-dontwarn androidx.test.espresso.**


###############
# Required for Mockito
-keep class retrofit2.NextcloudRetrofitApiBuilder { *; }
-keep class net.bytebuddy.* { *; }
-dontwarn net.bytebuddy.**

-keep class module-info
-keepattributes Module*
-dontwarn org.mockito.**


# Proguard rules that are applied to your test apk/code.
-ignorewarnings

-keepattributes *Annotation*

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter

-dontwarn androidx.concurrent.futures.AbstractResolvableFuture
-dontwarn org.conscrypt.Conscrypt
#com.google.common.util.concurrent.ListenableFuture
-keep interface okhttp3.internal.platform.ConscryptPlatform
-keep class okhttp3.internal.platform.ConscryptPlatform

-keep class org.conscrypt.Conscrypt { *; }
-keep interface org.conscrypt.Conscrypt { *; }