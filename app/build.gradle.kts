//import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
}

android {
    namespace = "com.v.v_notes"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }

    defaultConfig {
        applicationId = "com.v.v_notes"
        minSdk = 30
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.material3)
    //room
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.test)
    implementation(libs.androidx.benchmark.traceprocessor)
    implementation(libs.volley)
    ksp("androidx.room:room-compiler:2.8.4")
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.gson)

    implementation("javax.inject:javax.inject:1")

    //编辑器
    implementation("com.mohamedrejeb.richeditor:richeditor-compose-coil3:1.0.0-rc13")
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc13")

    //图片编辑
    implementation("com.burhanrashid52:photoeditor:3.1.0")

    implementation("com.github.bumptech.glide:glide:5.0.7")

    //图片查看
    //implementation("com.github.skydoves:imageviewer:2.2.1")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    //导航组件
    implementation("androidx.navigation:navigation-compose:2.7.7")

    //图片加载
    implementation("io.coil-kt:coil-compose:2.6.0")

    //数据库云端同步
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")

    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

    //Kotlin序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    //协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Material Design Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}