plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

android {
    android {
        useLibrary("org.apache.http.legacy")
    }

    namespace = "com.app.gamenews"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.gamenews"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.android.volley:volley:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.intuit.sdp:sdp-android:1.0.6")
    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    implementation ("com.google.firebase:firebase-firestore:24.7.1")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.google.firebase:firebase-crashlytics:18.3.1")

    implementation ("com.google.android.play:core:1.10.3")

    implementation ("androidx.multidex:multidex:2.0.1")


    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("org.aviran.cookiebar2:cookiebar2:1.1.5")

    implementation ("com.github.bumptech.glide:glide:4.13.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("com.karumi:dexter:6.0.1")

    implementation ("io.insert-koin:koin-android:3.2.0-beta-1")
    implementation ("io.insert-koin:koin-androidx-navigation:3.2.0-beta-1")
    testImplementation("io.insert-koin:koin-test-junit4:3.2.0-beta-1")
    implementation ("android.arch.persistence.room:runtime:1.1.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    implementation ("com.airbnb.android:lottie:5.2.0")

    implementation ("com.github.yunusemresorkac:FastPrefs:1.0")
    implementation ("com.github.Spikeysanju:MotionToast:1.4")
    implementation ("com.github.chivorns:smartmaterialspinner:1.5.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.firebase:firebase-iid:21.1.0")
    implementation ("com.google.android.gms:play-services-ads:22.5.0")
    implementation ("dev.shreyaspatil.MaterialDialog:MaterialDialog:2.2.3")


}