plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.spellweave"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.spellweave"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
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
    // app
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.gridlayout:gridlayout:1.1.0")

    // ---- Unit tests (JVM / Robolectric) ----
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13") // or latest stable
    testImplementation("androidx.test:core:1.6.1")        // for ApplicationProvider
    testImplementation("androidx.arch.core:core-testing:2.2.0") // handy for LiveData tests
    testImplementation("androidx.navigation:navigation-testing:2.6.0")


    // FragmentScenario needs EmptyFragmentActivity from the AAR manifest -> put on debug classpath
    debugImplementation("androidx.fragment:fragment-testing:1.8.2")

    // ---- Instrumentation tests (optional, if you run Espresso in src/androidTest) ----
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    androidTestImplementation("androidx.navigation:navigation-testing:2.6.0")

    androidTestImplementation("org.mockito:mockito-android:5.12.0")

// orchestrator support
    androidTestUtil("androidx.test:orchestrator:1.5.0")

}