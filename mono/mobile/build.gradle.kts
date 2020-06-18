/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("io.fabric")
    id("dagger.hilt.android.plugin")
    // id("androidx.benchmark")
}

android {

    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
        applicationId = "com.google.samples.apps.iosched"
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)
        versionCode = Versions.versionCodeMobile
        versionName = Versions.versionName

        testInstrumentationRunner = "com.google.samples.apps.iosched.tests.CustomTestRunner"
        //testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("com.google.android.gms.maps.model.LatLng",
                "MAP_VIEWPORT_BOUND_NE",
                "new com.google.android.gms.maps.model.LatLng(${properties["map_viewport_bound_ne"]})")
        buildConfigField("com.google.android.gms.maps.model.LatLng",
                "MAP_VIEWPORT_BOUND_SW",
                "new com.google.android.gms.maps.model.LatLng(${properties["map_viewport_bound_sw"]})")

        // ### Shared Module
        buildConfigField("String", "CONFERENCE_TIMEZONE", properties["conference_timezone"] as String)
        buildConfigField("String", "CONFERENCE_DAY1_START", properties["conference_day1_start"] as String)
        buildConfigField("String", "CONFERENCE_DAY1_END", properties["conference_day1_end"] as String)
        buildConfigField("String", "CONFERENCE_DAY2_START", properties["conference_day2_start"] as String)
        buildConfigField("String", "CONFERENCE_DAY2_END", properties["conference_day2_end"] as String)
        buildConfigField("String", "CONFERENCE_DAY3_START", properties["conference_day3_start"] as String)
        buildConfigField("String", "CONFERENCE_DAY3_END", properties["conference_day3_end"] as String)

        buildConfigField("String", "CONFERENCE_DAY1_AFTERHOURS_START", properties["conference_day1_afterhours_start"] as String)
        buildConfigField("String", "CONFERENCE_DAY2_CONCERT_START", properties["conference_day2_concert_start"] as String)

        buildConfigField("String",
                "BOOTSTRAP_CONF_DATA_FILENAME", properties["bootstrap_conference_data_filename"] as String)

        buildConfigField("String",
                "CONFERENCE_WIFI_OFFERING_START", properties["conference_wifi_offering_start"] as String)


        buildConfigField("float", "MAP_CAMERA_FOCUS_ZOOM", properties["map_camera_focus_zoom"] as String)

        resValue("dimen", "map_camera_bearing", properties["map_default_camera_bearing"] as String)
        resValue("dimen", "map_camera_target_lat", properties["map_default_camera_target_lat"] as String)
        resValue("dimen", "map_camera_target_lng", properties["map_default_camera_target_lng"] as String)
        resValue("dimen", "map_camera_tilt", properties["map_default_camera_tilt"] as String)
        resValue("dimen", "map_camera_zoom", properties["map_default_camera_zoom"] as String)
        resValue("dimen", "map_viewport_min_zoom", properties["map_viewport_min_zoom"] as String)
        resValue("dimen", "map_viewport_max_zoom", properties["map_viewport_max_zoom"] as String)

        manifestPlaceholders = mapOf("crashlyticsEnabled" to true)

        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            // TODO: b/120517460 shrinkResource can't be used with dynamic-feature at this moment.
            //       Need to ensure the app size has not increased
            manifestPlaceholders = mapOf("crashlyticsEnabled" to true)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string",
                    "google_maps_key",
                    "AIzaSyD5jqwKMm1SeoYsW25vxCXfTlhDBeZ4H5c")

            buildConfigField("String", "MAP_TILE_URL_BASE", "\"https://storage.googleapis.com/io2019-festivus-prod/images/maptiles\"")


            // ### Shared Module
            buildConfigField("String", "REGISTRATION_ENDPOINT_URL", "\"https://events-d07ac.appspot.com/_ah/api/registration/v1/register\"")
            buildConfigField("String", "CONFERENCE_DATA_URL", "\"https://firebasestorage.googleapis.com/v0/b/io2019-festivus-prod/o/sessions.json?alt=media&token=89140adf-e228-45a5-9ae3-8ed01547166a\"")
        }
        getByName("debug") {
            (this as ExtensionAware).extra["alwaysUpdateBuildId"] = false
            versionNameSuffix = "-debug"
            manifestPlaceholders = mapOf("crashlyticsEnabled" to false)
            resValue("string",
                    "google_maps_key",
                    "AIzaSyAhJx57ikQH9rYc8IT8W3d2As5cGHMBvuo")

            buildConfigField("String", "MAP_TILE_URL_BASE", "\"https://storage.googleapis.com/io2019-festivus/images/maptiles\"")


            // ### Shared Module
            buildConfigField("String", "REGISTRATION_ENDPOINT_URL", "\"https://events-dev-62d2e.appspot.com/_ah/api/registration/v1/register\"")
            buildConfigField("String", "CONFERENCE_DATA_URL", "\"https://firebasestorage.googleapis.com/v0/b/io2019-festivus/o/sessions.json?alt=media&token=019af2ec-9fd1-408e-9b86-891e4f66e674\"")
        }
        maybeCreate("staging")
        getByName("staging") {
            (this as ExtensionAware).extra["alwaysUpdateBuildId"] = false
            initWith(getByName("debug"))
            versionNameSuffix = "-staging"

            // Specifies a sorted list of fallback build types that the
            // plugin should try to use when a dependency does not include a
            // "staging" build type.
            // Used with :test-shared, which doesn't have a staging variant.
            matchingFallbacks = listOf("debug")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    signingConfigs {
        // We need to sign debug builds with a debug key to make firebase auth happy
        getByName("debug") {
            storeFile = file("../debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    // debug and release variants share the same source dir
    sourceSets {
        getByName("debug") {
            java.srcDir("src/debugRelease/java")
        }
        getByName("release") {
            java.srcDir("src/debugRelease/java")
        }
    }

    lintOptions {
        // Eliminates UnusedResources false positives for resources used in DataBinding layouts
        isCheckGeneratedSources = true
        // Running lint over the debug variant is enough
        isCheckReleaseBuilds = false
        // See lint.xml for rules configuration

        disable("InvalidPackage", "MissingTranslation")
        // Version changes are beyond our control, so don't warn. The IDE will still mark these.
        disable("GradleDependency")
        // Timber needs to update to new Lint API
        disable("ObsoleteLintCustomCheck")
    }

    testBuildType = "staging"

    // Required for AR because it includes a library built with Java 8
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // To avoid the compile error: "Cannot inline bytecode built with JVM target 1.8
    // into bytecode that is being built with JVM target 1.6"
    kotlinOptions {
        val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
}

dependencies {
    api(platform(project(":depconstraints")))
    kapt(platform(project(":depconstraints")))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Libs.CORE_KTX)

    // UI
    implementation(Libs.ACTIVITY_KTX)
    implementation(Libs.APPCOMPAT)
    implementation(Libs.FRAGMENT_KTX)
    implementation(Libs.CARDVIEW)
    implementation(Libs.BROWSER)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.DRAWER_LAYOUT)
    implementation(Libs.MATERIAL)
    implementation(Libs.FLEXBOX)
    implementation(Libs.LOTTIE)
    implementation(Libs.INK_PAGE_INDICATOR)

    // Architecture Components
    implementation(Libs.LIFECYCLE_LIVE_DATA_KTX)
    kapt(Libs.LIFECYCLE_COMPILER)
    testImplementation(Libs.ARCH_TESTING)
    implementation(Libs.NAVIGATION_FRAGMENT_KTX)
    implementation(Libs.NAVIGATION_UI_KTX)
    implementation(Libs.ROOM_KTX)
    implementation(Libs.ROOM_RUNTIME)
    kapt(Libs.ROOM_COMPILER)
    testImplementation(Libs.ROOM_KTX)
    testImplementation(Libs.ROOM_RUNTIME)

    // Dagger Hilt
    implementation(Libs.HILT_ANDROID)
    implementation(Libs.HILT_VIEWMODEL)
    androidTestImplementation(Libs.HILT_TESTING)
    kapt(Libs.HILT_COMPILER)
    kapt(Libs.ANDROIDX_HILT_COMPILER)
    kaptAndroidTest(Libs.HILT_COMPILER)
    kaptAndroidTest(Libs.ANDROIDX_HILT_COMPILER)

    // Glide
    implementation(Libs.GLIDE)
    kapt(Libs.GLIDE_COMPILER)

    // Fabric and Firebase
    implementation(Libs.FIREBASE_UI_AUTH)
    implementation(Libs.CRASHLYTICS)

    // Date and time API for Java.
    implementation(Libs.THREETENABP)
    testImplementation(Libs.THREETENBP)

    // Kotlin
    implementation(Libs.KOTLIN_STDLIB)

    // Instrumentation tests
    androidTestImplementation(Libs.ESPRESSO_CORE)
    androidTestImplementation(Libs.ESPRESSO_CONTRIB)
    androidTestImplementation(Libs.EXT_JUNIT)
    androidTestImplementation(Libs.RUNNER)
    androidTestImplementation(Libs.RULES)

    // Local unit tests
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.MOCKITO_CORE)
    testImplementation(Libs.MOCKITO_KOTLIN)
    testImplementation(Libs.HAMCREST)

    // Solve conflicts with gson. DataBinding is using an old version.
    implementation(Libs.GSON)

    implementation(Libs.ARCORE)

    // ###AR Module
    implementation(Libs.GOOGLE_PLAY_SERVICES_VISION)

    // ###Benchmark Module
    androidTestImplementation(Libs.BENCHMARK)
    androidTestImplementation(Libs.HAMCREST)

    // ###Shared Module
    // Architecture Components
    implementation(Libs.LIFECYCLE_VIEW_MODEL_KTX)
    // Maps
    api(Libs.GOOGLE_MAP_UTILS) {
        exclude(group = "com.google.android.gms")
    }
    api(Libs.GOOGLE_PLAY_SERVICES_MAPS)
    api(Libs.TIMBER)
    // OkHttp
    implementation(Libs.OKHTTP)
    implementation(Libs.OKHTTP_LOGGING_INTERCEPTOR)
    // Has to be replaced to avoid compile / runtime conflicts between okhttp and firestore
    api(Libs.OKIO)
    api(Libs.FIREBASE_AUTH)
    api(Libs.FIREBASE_CONFIG)
    api(Libs.FIREBASE_ANALYTICS)
    api(Libs.FIREBASE_FIRESTORE)
    api(Libs.FIREBASE_FUNCTIONS)
    api(Libs.FIREBASE_MESSAGING)
    // Coroutines
    api(Libs.COROUTINES)
    testImplementation(Libs.COROUTINES_TEST)


    // ###Model Module
    // ThreeTenBP for the shared module only. Date and time API for Java.
    api("org.threeten:threetenbp:${Versions.THREETENBP}:no-tzdb")
}

apply(plugin = "com.google.gms.google-services")
