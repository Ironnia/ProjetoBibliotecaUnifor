// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    // Add the dependency for the Crashlytics Gradle plugin
    // https://firebase.google.com/docs/crashlytics/android/get-started?hl=pt-br#add-plugin
    id("com.google.firebase.crashlytics") version "3.0.7" apply false

}