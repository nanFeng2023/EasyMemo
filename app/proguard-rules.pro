-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-obfuscationdictionary proguard-my3.txt
-classobfuscationdictionary proguard-my3.txt
-packageobfuscationdictionary proguard-my3.txt

-keep class com.adjust.sdk.**{ *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.**{ *; }

