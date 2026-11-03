# Gson DTOs (reflection)
-keep class com.sandalia.stockanchor.data.remote.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Room entities are serialised with Gson for the Basic → Premium upload (SyncManager.uploadLocal):
# their field names are the JSON keys the server reads, so R8 must not rename them.
-keepclassmembers class com.sandalia.stockanchor.data.local.*Entity { <fields>; }
