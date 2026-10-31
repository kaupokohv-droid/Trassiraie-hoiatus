plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "ee.rmk.trassihoiatus"; compileSdk = 35
 defaultConfig { applicationId = "ee.rmk.trassihoiatus"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1" } }

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
