import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

val projectJvmTarget = "11"

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.versions)
    base
}

allprojects {
    group = PUBLISHING_GROUP
}

val ktlintVersion = libs.versions.ktlint.asProvider().get()
val detektFormatting = libs.detekt.formatting

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

subprojects {
    val ktlint: Configuration by configurations.creating

    val ktlintCheck by tasks.creating(JavaExec::class) {
        inputs.files(inputFiles)
        outputs.dir(outputDir)

        description = "Check Kotlin code style."
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        args = listOf("src/**/*.kt")
    }

    val ktlintFormat by tasks.creating(JavaExec::class) {
        inputs.files(inputFiles)
        outputs.dir(outputDir)

        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        args = listOf("-F", "src/**/*.kt")
    }

    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("com.diffplug.spotless")
    }

    detekt {
        config = rootProject.files("config/detekt/detekt.yml")
    }

    spotless {
        kotlin {
            target(
                fileTree(
                    mapOf(
                        "dir" to ".",
                        "include" to listOf("**/*.kt"),
                        "exclude" to listOf("**/build/**", "**/spotless/*.kt")
                    )
                )
            )
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
            val delimiter = "^(package|object|import|interface|internal|@file|//startfile)"
            val licenseHeaderFile = rootProject.file("spotless/copyright.kt")
            licenseHeaderFile(licenseHeaderFile, delimiter)
        }
    }

    dependencies {
        detektPlugins(detektFormatting)
        ktlint("com.pinterest:ktlint:0.45.2")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = projectJvmTarget
                freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental"
                freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.FlowPreview"
                freeCompilerArgs =
                    freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            }
        }
    }
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            candidate.version.isStableVersion().not()
        }
    }
}
