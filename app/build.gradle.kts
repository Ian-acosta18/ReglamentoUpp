plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.reglamentoupp"
    // --- CORRECCIÓN 1: Actualizado a 36 ---
    compileSdk = 36 //

    defaultConfig {
        applicationId = "com.example.reglamentoupp"
        minSdk = 24
        // --- CORRECCIÓN 1: Actualizado a 36 ---
        targetSdk = 36 //
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
    // PEGA ESTE CÓDIGO EN SU LUGAR
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- CORRECCIÓN 2: Dependencias de Firebase correctas ---

    // 1. Añade el "Bill of Materials" (BOM)
    // Esto asegura que todas tus librerías de Firebase sean compatibles.
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // 2. Añade las librerías que tu CÓDIGO SÍ USA:

    // Para FirebaseAuth.getInstance() en LoginActivity
    implementation("com.google.firebase:firebase-auth")

    // Para FirebaseFirestore.getInstance() en LoginActivity, MainActivity, y GameLevelActivity
    implementation("com.google.firebase:firebase-firestore")

    // 3. Esta era la librería INCORRECTA (es para Realtime Database, no Firestore)
    // implementation(libs.firebase.database) //

    // --- Fin de la Corrección ---

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.gridlayout:gridlayout:1.0.0")
}