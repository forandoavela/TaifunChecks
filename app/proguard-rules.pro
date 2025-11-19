# ═══════════════════════════════════════════════
# ProGuard Rules for FlightChecks
# ═══════════════════════════════════════════════

# SnakeYAML - required for YAML parsing
-keep class org.yaml.snakeyaml.** { *; }
-dontwarn org.yaml.snakeyaml.**

# DataStore Preferences - required for settings persistence
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-keep class androidx.datastore.*.** { *; }

# Navigation Component - required for screen navigation
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.**

# Kotlin Coroutines - required for async operations
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Model/Domain classes - required for YAML serialization
-keep class com.taifun.checks.domain.** { *; }

# Jetpack Compose - required for UI
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Compose Compiler - required for @Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# AndroidX - general rules
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep debugging information for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures for reflection
-keepattributes Signature

# Keep all string resources (prevents localization resources from being stripped)
-keep class **.R$string { *; }
-keep class **.R$raw { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep locale-related classes
-keep class java.util.Locale { *; }
-keep class android.content.res.Configuration { *; }
-keep class android.content.res.AssetManager { *; }

# Remove logging in release builds (optional - uncomment to reduce APK size further)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
