plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
}

android {
    namespace = "net.k1ra.eizo_demo_app_android"
    compileSdk {
        //noinspection WrongGradleMethod
        val (major, minor) = libs.versions.android.compileSdk.get().split(".").map { it.toInt() } + listOf(0)
        version = release(major) { minorApiLevel = minor }
    }

    defaultConfig {
        applicationId = "net.k1ra.eizo_demo_app_android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":EizoDemo"))

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)

    implementation(libs.androidx.activity.compose)
}