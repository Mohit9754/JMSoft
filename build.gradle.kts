plugins {
    id("com.android.application") version "8.3.0" apply false
    kotlin("android") version "1.8.21" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}