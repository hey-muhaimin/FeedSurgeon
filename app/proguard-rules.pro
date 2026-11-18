# Feed Surgeon R8 / ProGuard Rules

-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Keep AccessibilityService registered in AndroidManifest.xml
-keep class com.example.service.FeedSurgeonService { *; }

# Moshi codegen adapters & DTOs
-keep class *JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-keep class com.example.data.model.** { *; }

# Room entities and database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }

# Coroutines & OkHttp safe warnings
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
