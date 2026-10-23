# Default ProGuard rules. Add app-specific rules below.

# ── auth-service wire models ──────────────────────────────────────────────────
# Gson binds these by field name, so the names must survive shrinking even though
# nothing in the app reads the request fields directly.
-keep class com.saraswati.institute.network.AuthRequests$* { <fields>; }
-keepclassmembers class com.saraswati.institute.network.TokenResponse { <fields>; }
-keepclassmembers class com.saraswati.institute.network.UserResponse { <fields>; }
-keepclassmembers class com.saraswati.institute.network.RegistrationResponse { <fields>; }
-keepclassmembers class com.saraswati.institute.network.MessageResponse { <fields>; }
-keepclassmembers class com.saraswati.institute.network.TokenValidationResponse { <fields>; }
-keepclassmembers class com.saraswati.institute.network.ErrorResponse { <fields>; }

# ── Retrofit / OkHttp / Gson ──────────────────────────────────────────────────
# Retrofit reads the generic return types of the service interface through reflection.
-keep,allowobfuscation,allowshrinking interface com.saraswati.institute.network.AuthApiService
-keep,allowobfuscation,allowshrinking class kotlin.Metadata
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
