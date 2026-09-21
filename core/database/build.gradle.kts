plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.davidegea.spotifystats.database"
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

room {
    schemaDirectory(projectDir.resolve("schemas").path)
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.17")
    implementation(project(":core:model"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
