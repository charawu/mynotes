// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    kotlin("jvm") version "2.2.10" apply false // 通常已在根构建脚本中定义
    id("com.google.devtools.ksp") version "2.3.6" apply false

//    id("com.android.application") version "9.1.1" apply false
//    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
//    id("com.android.legacy.kapt") version "9.1.1" apply false
}