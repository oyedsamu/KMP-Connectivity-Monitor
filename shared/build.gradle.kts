import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // ✅ add linker options for all iOS targets
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            linkerOpts("-framework", "Network")
        }
    }
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            linkerOpts("-framework", "SystemConfiguration")
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val iosTest by getting
        val androidUnitTest by getting

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("com.github.skydoves:landscapist-coil3:2.4.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

extensions.configure<LibraryExtension>("android") {
    namespace = "com.qandil.kmpconnectivity"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
