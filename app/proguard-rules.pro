# Add project specific ProGuard rules here.
# Keep WebView JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes
-keepclassmembers class android.webkit.** { *; }
-keep class androidx.webkit.** { *; }

# Keep app classes
-keep class com.farazai.notesapp.** { *; }
