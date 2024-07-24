// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        // Add the classpath for Google services
        classpath("com.google.gms:google-services:4.3.3")
    }
}

plugins {
    // Apply the Kotlin plugin to the project
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

//allprojects {
//    repositories {
//        mavenCentral()
//    }
//}
