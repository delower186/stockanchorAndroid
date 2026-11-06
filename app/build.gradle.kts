import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Release signing: keep the keystore and its passwords OUT of git, in keystore.properties (project root):
//   storeFile=/home/you/keys/stockanchor-upload.jks
//   storePassword=...
//   keyAlias=upload
//   keyPassword=...
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.sandalia.stockanchor"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sandalia.stockanchor"
        minSdk = 26
        targetSdk = 37
        // Bump versionCode on every Play upload
        versionCode = 7
        versionName = "v1.0.7"

        // Base URL of the Laravel API (must end with /api/). 10.0.2.2 = host machine from the emulator.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/api/\"")
        // Google Play subscription product and its base plans (Play Console → Monetize → Subscriptions).
        buildConfigField("String", "SUBSCRIPTION_PRODUCT_ID", "\"sa_premium\"")
        buildConfigField("String", "PLAN_MONTHLY", "\"monthly\"")
        buildConfigField("String", "PLAN_YEARLY", "\"yearly\"")
        // Developer-determined offer on both base plans (Play Console → the base plan → Add offer), free for P1M.
        buildConfigField("String", "TRIAL_OFFER_ID", "\"free-trial-1-month\"")
        // Company web panel opened from More → "Open web panel" (Premium only)
        buildConfigField("String", "WEB_PANEL_URL", "\"https://stockanchor.sandalia.bd\"")
    }

    signingConfigs {
        if (keystoreProps.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Lets you test Premium without Play Billing against an API running SUBSCRIPTION_VERIFY_MODE=fake.
            buildConfigField("boolean", "ALLOW_TEST_PURCHASES", "true")
            // Settings → Cloud server lets developers point the app at another API
            buildConfigField("boolean", "ALLOW_SERVER_OVERRIDE", "true")
            // Plain http to the local dev server (10.0.2.2:8000)
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Our cloud. Fixed in the app: customers never see or change it.
            buildConfigField("String", "API_BASE_URL", "\"https://stockanchor.sandalia.bd/api/\"")
            buildConfigField("boolean", "ALLOW_TEST_PURCHASES", "false")
            buildConfigField("boolean", "ALLOW_SERVER_OVERRIDE", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
    // The in-app language picker (Settings → Language) needs every language inside the installed app,
    // so Play must not split the bundle by language.
    bundle {
        language {
            enableSplit = false
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.core:core-splashscreen:1.2.0")
    // Play Services pulls in an old Fragment that breaks registerForActivityResult (permission prompt)
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // Room (Basic local database + Premium offline cache). 2.7+ is required by KSP2 (KSP 2.3+).
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // Cloud API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Background work (low stock alerts)
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Subscriptions
    implementation("com.android.billingclient:billing-ktx:8.3.0")

    // Barcode scanning (Google code scanner, no camera permission needed)
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
