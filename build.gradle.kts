import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "com.m"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


application {
    mainClass.set("pack.MainKt")
}


val libgdxVersion = "1.11.0"
val log4jVersion = "2.17.2"
dependencies {

    // core
    implementation("com.badlogicgames.gdx:gdx:$libgdxVersion")


    // bullet
    implementation("com.badlogicgames.gdx:gdx-bullet:$libgdxVersion")
    implementation("com.badlogicgames.gdx:gdx-bullet-platform:$libgdxVersion:natives-desktop")

    // desktop render
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$libgdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$libgdxVersion:natives-desktop")


    // log4j2
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}