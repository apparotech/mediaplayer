

plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.video_player"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.apparotech.video_player"
        minSdk = 26
        targetSdk = 33
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
     signingConfigs {
        create("release") {
            

        }
     }


    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false



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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //-------------------------EXOPLAYER----------------
    implementation ("com.google.android.exoplayer:exoplayer:2.14.0")
    //---------------Swiperefreshlayout----------------------
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
//------ Glide library--------
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    //FILE PICKER
    implementation ("com.github.angads25:filepicker:1.1.1")

    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.0-rc01")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
}