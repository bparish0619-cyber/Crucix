# Crucix Android ProGuard Rules

# Keep Security Crypto
-keep class androidx.security.crypto.** { *; }

# Keep Tink (used by EncryptedSharedPreferences)
-keep class com.google.crypto.tink.** { *; }

# Keep data classes
-keep class com.crucix.android.data.** { *; }
