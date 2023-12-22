plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.accessibilityverifier"
    compileSdk = 34

    defaultConfig {
        android.buildFeatures.buildConfig = true
        applicationId = "com.example.accessibilityverifier"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val AXE_DEVTOOLS_APIKEY = "bf7811a1-e1ec-4576-af1d-5dbdc369ea9d"
        buildConfigField("String", "AXE_DEVTOOLS_APIKEY", "\"$AXE_DEVTOOLS_APIKEY\"")
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.deque.android:axe-android:0.2.0")
    implementation("com.google.code.gson:gson:2.10.1")
    //implementation("com.deque.android:axe-devtools-android:4.3.0")
    implementation("androidx.test:monitor:1.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0-alpha05")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    //implementation("com.google.android.gms:play-services-tts:23.0.0")
    implementation("eu.bolt:screenshotty:1.0.4")
}
