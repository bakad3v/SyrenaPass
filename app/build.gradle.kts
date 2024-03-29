plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    kotlin("plugin.serialization")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.android.syrenapass"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.syrenapass"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

  buildFeatures {
    dataBinding = true
    viewBinding = true
  }

}

dependencies {
  val daggerVersion = "2.48"
  val datastoreVersion = "1.0.0"
  val roomVersion = "2.6.1"
  val runtimeKtxVersion = "2.9.0"
  val lifecycleVersion = "2.7.0"
  val sqlCipherVersion = "4.4.0"
  val sqliteVersion = "2.4.0"
  val coroutinesVersion = "1.7.3"
  val navigationVersion = "2.7.7"
  val fragmentVersion = "1.6.2"
  val hiltworkerVersion = "1.1.0"
  val securityVersion = "1.1.0-alpha06"
  val desugaringVersion = "2.0.4"
  val hiltVersion = "2.48"
  val leakCanaryVersion = "2.12"
  val coilVersion = "2.1.0"
  val storageVersion = "1.5.5"
  val serializeVersion = "1.6.2"
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.11.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  implementation ("com.google.dagger:dagger:$daggerVersion")
  kapt ("com.google.dagger:dagger-compiler:$daggerVersion")
  implementation ("androidx.datastore:datastore-preferences:$datastoreVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation ("androidx.work:work-runtime-ktx:$runtimeKtxVersion")
  implementation( "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
  implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  kapt ("androidx.room:room-compiler:$roomVersion")
  implementation ("androidx.room:room-runtime:$roomVersion")
  implementation ("net.zetetic:android-database-sqlcipher:$sqlCipherVersion")
  implementation( "androidx.sqlite:sqlite-ktx:$sqliteVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
  implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
  implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
  implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
  implementation ("androidx.hilt:hilt-work:$hiltworkerVersion")
  kapt ("androidx.hilt:hilt-compiler:$hiltworkerVersion")
  implementation( "androidx.security:security-crypto-ktx:$securityVersion")
  coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:$desugaringVersion")
  debugImplementation ("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
  implementation ("com.google.dagger:hilt-android:$hiltVersion")
  kapt ("com.google.dagger:hilt-compiler:$hiltVersion")
  implementation("io.coil-kt:coil:$coilVersion")
  implementation ("com.anggrayudi:storage:$storageVersion")
  api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializeVersion")
}
