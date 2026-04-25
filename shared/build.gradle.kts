import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    android {
        namespace = "com.qandil.kmpconnectivity.shared"
        compileSdk = 36
        minSdk = 24
        androidResources {
            enable = true
        }
        withHostTest {}
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
        val androidHostTest by getting

        commonMain.dependencies {
            implementation("org.jetbrains.compose.ui:ui:${libs.versions.jetbrains.compose.get()}")
            implementation("org.jetbrains.compose.runtime:runtime:${libs.versions.jetbrains.compose.get()}")
            implementation("org.jetbrains.compose.foundation:foundation:${libs.versions.jetbrains.compose.get()}")
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
            implementation("com.github.skydoves:landscapist-coil3:2.4.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidHostTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
