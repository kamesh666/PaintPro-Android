# Add project specific ProGuard rules here.
# Minification is disabled for this build (see app/build.gradle.kts), so these
# rules only matter once you turn isMinifyEnabled = true on for a release build.

-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class com.paintpro.app.**$$serializer { *; }
-keepclassmembers class com.paintpro.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.paintpro.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
