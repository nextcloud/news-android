# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-studio/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

# Required for Test execution
-dontwarn org.xmlpull.v1.**
-dontwarn org.apache.tools.ant.**
-dontwarn java.beans.**
-dontwarn javax.naming.**
-dontwarn sun.misc.Unsafe


# Mockito
-dontwarn org.mockito.**


-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


# AndroidSlidingUpPanel
# https://github.com/umano/AndroidSlidingUpPanel/issues/921
-dontwarn com.sothree.slidinguppanel.SlidingUpPanelLayout

# Gson
# Following are rules from
# https://github.com/google/gson/blob/37ed0fcbd7930df0aad9f5068c608e4465413877/examples/android-proguard-example/proguard.cfg
# no rules are packaged into gson (as of 2.10.1)
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.nextcloud.android.sso.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------

# Retrofit
# Following are additional rules not released yet (as of 2.9.0)
# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# rxjava3
# https://github.com/square/retrofit/blob/ac07d9162309b11a3b8b3f14909b5d05d3f021d0/retrofit-adapters/rxjava3/src/main/resources/META-INF/proguard/retrofit2-rxjava3-adapter.pro
# Keep generic signature of RxJava3 (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking class io.reactivex.rxjava3.core.Flowable
-keep,allowobfuscation,allowshrinking class io.reactivex.rxjava3.core.Maybe
-keep,allowobfuscation,allowshrinking class io.reactivex.rxjava3.core.Observable
-keep,allowobfuscation,allowshrinking class io.reactivex.rxjava3.core.Single




# Other Libraries
-dontwarn org.apache.velocity.**
-dontwarn freemarker.**
-dontwarn com.google.auto.value.**
-dontwarn autovalue.shaded.**
#-keep class com.gu.option.Option
#-keep class com.gu.option.UnitFunction

-keep class de.luhmer.** { *; }
-keepclassmembers class de.luhmer.** { *; }

-printmapping out.map
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepclasseswithmembers public class android.support.v7.widget.RecyclerView { *; }


###############
# GreenDAO
-keep class de.greenrobot.** { *; }
-dontwarn de.greenrobot.daogenerator.DaoGenerator

-keepclassmembers class * extends de.greenrobot.dao.AbstractDao { *; }


###############
# Guava (official)
## Not yet defined: follow https://github.com/google/guava/issues/2117
# Guava (unofficial)
## https://github.com/google/guava/issues/2926#issuecomment-325455128
## https://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn com.google.common.base.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn java.lang.ClassValue
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Added for guava 23.5-android
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**


# Required for unit tests

# https://stackoverflow.com/a/39777485
# Also, note that this rule should be added to the regular proguard file(the one of listed in proguardFiles) and not the test one(declared as testProguardFile)
# java.lang.NoSuchMethodError: No virtual method getParameter
-keepclasseswithmembers public class com.nextcloud.android.sso.aidl.NextcloudRequest { *; }
-keepclasseswithmembers public class com.nextcloud.android.sso.AccountImporter { *; }

# NewsReaderListActivityTests
-keepclasseswithmembers public class androidx.recyclerview.widget.RecyclerView { *; }
