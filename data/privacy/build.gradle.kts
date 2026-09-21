plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.davidegea.spotifystats.data.privacy"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    testOptions { unitTests.isIncludeAndroidResources = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation(project(":core:model"))
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.17")
    implementation(project(":core:database"))
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.ktx)
}
