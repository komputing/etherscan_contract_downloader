apply {
    from("https://raw.githubusercontent.com/ligi/gradle-common/master/versions_plugin_stable_only.gradle")
}

buildscript {
    repositories {
        jcenter()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.github.ben-manes:gradle-versions-plugin:${Versions.versions_plugin}")
    }
}

apply(plugin = "kotlin")
plugins {
    application
}

application {
    mainClassName = "org.komputing.etherscan.downloader.DownloaderKt"
}

repositories {
    jcenter()
}

dependencies {
    "implementation"("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    "implementation"("com.github.doyaaaaaken:kotlin-csv-jvm:0.7.3")
    "implementation"("com.github.kittinunf.fuel:fuel-moshi:2.2.1")
    "implementation"("com.squareup.moshi:moshi:${Versions.moshi}")
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")

    "testImplementation"("org.assertj:assertj-core:3.14.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.jupiter}")
    "testRuntime"("org.junit.jupiter:junit-jupiter-engine:${Versions.jupiter}")

    "testImplementation"("org.jetbrains.kotlin:kotlin-test")
    "testImplementation"("io.mockk:mockk:1.9.3")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}
