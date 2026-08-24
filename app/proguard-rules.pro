# Keep Room Entities and Data Models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.dao.** { *; }
-keep class com.example.data.database.** { *; }

# Keep Firebase models
-keepclassmembers class * {
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
}

# Keep Moshi JSON classes
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

