@file:Suppress("UnstableApiUsage")
plugins {
    id("com.android.application")
    //  id("com.google.gms.google-services")
    // Add the Crashlytics Gradle plugin
    kotlin("android")
}

android {
    namespace = "com.jmsoft"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.jmsoft"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-${System.getenv("VERSION_SHA")}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false

            debug {
                isDebuggable = true
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )         // Other release configurations

        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

kotlin {
    jvmToolchain(17)
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.activity:activity:1.9.0")
    implementation(files("libs/poishadow-all.jar"))

    implementation (files("libs/EZioAD.jar"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //sdp dependency
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:21.0.0")

    implementation("com.google.zxing:core:3.4.1")

    implementation ("com.joanzapata.pdfview:android-pdfview:1.0.4@aar")

    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")

    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("com.karumi:dexter:6.2.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    //generate a QR code  library

    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")


//    implementation (files("libs/poishadow-all.jar"))

    // Validation For Phone Number
    implementation("io.michaelrocks:libphonenumber-android:8.12.49")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3")
    //Google
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("no.nordicsemi.android:dfu:1.9.0") {
        exclude(group = "com.android.support")
    }
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}

