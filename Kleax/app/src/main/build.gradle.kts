//COMPARAR CON EL ARCHIVO PASADO

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kleax"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kleax"
        minSdk = 31
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    val implementation = implementation(libs.constraintlayout)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0") //desde aqui son las dependencias para pocketbase
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // Agregar Glide aquí
    implementation("com.github.bumptech.glide:glide:4.15.1")  // Dependencia de Glide
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")  // Dependencia para la anotación de Glide

}
