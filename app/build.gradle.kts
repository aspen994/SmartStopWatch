import java.nio.charset.Charset
import java.util.UUID


plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.osinTechInnovation.smartstopwatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.osinTechInnovation.smartstopwatch"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

        sourceSets.getByName("main") {
            assets.srcDirs("$buildDir/generated/assets")
        }



}

dependencies {

    implementation ("com.alphacephei:vosk-android:0.3.47@aar")

    implementation (group = "com.alphacephei", name= "vosk-android", version = "0.3.32+")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.android.gms:play-services-ads:23.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


tasks.register("genUUID"){
    val uuid = UUID.randomUUID().toString()
    val odir = file("$buildDir/generated/assets/model-en-us")
    val ofile = file("$odir/uuid")
    doLast {
        mkdir(odir)
        ofile.writeText(uuid, Charset.defaultCharset())
    }
}

tasks.named("preBuild") {
    dependsOn("genUUID")
}

