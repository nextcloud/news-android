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


# Other Libraries
-dontwarn org.apache.velocity.**
-dontwarn freemarker.**
-dontwarn com.google.auto.value.**
-dontwarn autovalue.shaded.**
#-keep class com.gu.option.Option
#-keep class com.gu.option.UnitFunction

# keep application classes used as database and network models
-keep class de.luhmer.owncloudnewsreader.database.model.** { *; }
-keep class de.luhmer.owncloudnewsreader.reader.nextcloud.ItemIds { *; }
-keep class de.luhmer.owncloudnewsreader.reader.nextcloud.ItemMap { *; }
-keep class de.luhmer.owncloudnewsreader.model.** { *; }
# keep the name of SyncItemStateService so SyncItemStateService.isMyServiceRunning works
-keepnames class de.luhmer.owncloudnewsreader.services.SyncItemStateService
# keep fields necessary for NewsReaderListActivity.adjustEdgeSizeOfDrawer and NewsReaderListActivity.getEdgeSizeOfDrawer to work
-keepclassmembers class androidx.drawerlayout.widget.DrawerLayout {
    private androidx.customview.widget.ViewDragHelper mLeftDragger;
}
-keepclassmembers class androidx.customview.widget.ViewDragHelper {
    private int mEdgeSize;
}

-printmapping out.map
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

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
