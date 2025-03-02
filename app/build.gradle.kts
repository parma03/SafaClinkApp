plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.safaclink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safaclink"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.android.networking)
    implementation (libs.popup.dialog)
    implementation(libs.spots.dialog)
    implementation (libs.databinding.runtime)
    implementation(libs.picasso)
    implementation(libs.swipereveallayout)

}