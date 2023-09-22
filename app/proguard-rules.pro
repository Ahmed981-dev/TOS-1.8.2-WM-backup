# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles-keep class * {
                                                                       ##    public private *;
                                                                       ##}
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-ignorewarnings
#-keep class * {
#    public private *;
#}

-obfuscationdictionary method-dictionary.txt
-packageobfuscationdictionary package-dictionary.txt
-classobfuscationdictionary class-dictionary.txt

-keep, allowobfuscation class com.android.services.*
-keepclassmembers, allowobfuscation class * {
    *;
}

-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.security.**
-keep class mailcap.** {*;}
-keep class mimetypes.** {*;}
-keep class myjava.awt.datatransfer.** {*;}
-keep class org.apache.harmony.awt.** {*;}
-keep class org.apache.harmony.misc.** {*;}
-keep class com.android.services.db.entities.** { *; }
-keep class com.android.services.models.** { *; }
-keep class com.android.services.enums.** { *; }

-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

#-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-android
-dontpreverify
# Optimization is turned off by default. Dex does not like code run # through the ProGuard optimize and preverify steps (and performs #some of these optimizations on its own).
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable
-optimizations !method/inlining/*
-optimizations !code/simplification/arithmetic
-optimizationpasses 5
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-useuniqueclassmembernames
-overloadaggressively
-allowaccessmodification

-repackageclasses 'o'
-assumenosideeffects
class android.util.Log {
public static *** d(...);
public static *** i(...);
public static *** v(...);
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v4.app.DialogFragment
-keep public class com.android.vending.licensing.ILicensingService
#For native methods, see #http://proguard.sourceforge.net/manual/examples.html
#native
#-keepclasseswithmembernames class * {
#native<methods> clMember;
#}
-keep public class * extends android.view.View {
public <init>(android.content.Context);
public <init>(android.content.Context, android.util.AttributeSet); public <init>(android.content.Context, android.util.AttributeSet, int);
public void set*(...);
}
#-keepclasseswithmembers class * {
#public <init>(android.content.Context, android.util.AttributeSet);
#}
#-keepclasseswithmembers class * {
#public <init>(android.content.Context, android.util.AttributeSet,
#int);
#}
-keepclassmembers class * extends android.app.Activity {
public void *(android.view.View);
}
#For enumeration classes, see
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    *;
}
#-keepclassmembers class **.R$* {
#public static <fields>;
#}
-keep class android.support.v4.app.** {*;}
-keep interface android.support.v4.app.** {*;}
#The support library contains references to newer platform versions.
#Don't warn about those in case this app is linking against an older
#platform version. We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn com.google.ads.**
#-keepclassmembers class * {
#@android.webkit.JavascriptInterface<methods>;
#}
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}
-keepclasseswithmembers class * {
public void onPayment*(...);
}

# Prevent Proguard from inlining methods that are intentionally extracted to ensure locals have a
# constrained liveness scope by the GC. This is needed to avoid keeping previous request references
# alive for an indeterminate amount of time. See also https://github.com/google/volley/issues/114
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.NetworkDispatcher {
    void processRequest();
}
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.CacheDispatcher {
    void processRequest();
}


-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.squareup.okhttp3.** {
*;
}

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Room database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

### Fabric
# In order to provide the most meaningful crash reports
-keepattributes SourceFile,LineNumberTable
# If you're using custom Eception
-keep public class * extends Exception

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

### Crash report
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

### Other
-dontwarn com.google.errorprone.annotations.*


### Kotlin Coroutine
# https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# https://github.com/Kotlin/kotlinx.atomicfu/issues/57
-dontwarn kotlinx.atomicfu.**

### Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keep class com.arthenica.mobileffmpeg.Config {
void log(long, int, byte[]);
void statistics(long, int, float, float, long , int, double, double);
}

-keep class com.arthenica.mobileffmpeg.AbiDetect {
}

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses