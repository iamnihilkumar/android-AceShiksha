# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.gemalto.jp2.JP2Decoder
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firestore
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Firebase Common
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firestore data model classes (IMPORTANT — your User, Quiz, Topic etc.)
-keep class com.nikhil.aceshiksha.models.** { *; }

# Gemini / Generative AI SDK
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Kotlin coroutines (used by Firebase and Gemini)
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Gemini network request/response models (Retrofit + Gson serialization)
-keep class com.nikhil.aceshiksha.network.** { *; }
-dontwarn com.nikhil.aceshiksha.network.**

# Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Gson (used by Retrofit to serialize request body)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepattributes SerializedName
-keepattributes Signature