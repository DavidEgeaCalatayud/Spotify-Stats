plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.davidegea.spotifystats.model"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
