# Keep Room Database entities and DAOs
-keep class com.example.core.database.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

# Keep Moshi JSON DTOs
-keep class com.example.core.network.** { *; }
-keep class com.example.core.model.** { *; }
-keepattributes Signature, *Annotation*

# Keep Retrofit Interfaces
-keepinterface com.example.core.network.RiotApiService

