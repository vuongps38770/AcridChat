import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.acrid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.acrid"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    val localProperties = Properties()
    val localPropertiesFile = File(rootDir,"local.properties")
    if(localPropertiesFile.isFile&&localPropertiesFile.exists()){
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug{
            buildConfigField("String", "FIREBASE_SOCKET_URL",localProperties.getProperty("FIREBASE_SOCKET_URL"))
            buildConfigField("String","APP_ID_AGORA", localProperties.getProperty("APP_ID_AGORA") )
        }

    }

    packaging{
        resources{
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
        }
    }

    buildFeatures{
        viewBinding=true
        dataBinding=true
        buildConfig=true
        resValues=true
    }
    viewBinding {
        enable = true
    }
    dataBinding {
        enable = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.core.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.google.firebase:firebase-auth")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-database")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.17.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.google.code.gson:gson:2.12.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("io.agora.rtc:voice-sdk:4.5.0")
    implementation ("com.github.Baseflow:PhotoView:2.3.0")





}