import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup:kotlinpoet:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val generatedResFilesPath = "$buildDir/generated/source/main"
val generatedResPackage = "generated.res"

tasks.register("buildResProcessor") {
    ResProcessorTask()
        .apply {
            androidStringsXmlPath = "$projectDir/androidApp/main/res/value/"
            androidDrawablesPath = "$projectDir/androidApp/main/res/drawable"
            androidDrawablePrefixFilter= "kmm_ic_"

            darwinLocalizationFolderPath = "$projectDir/iosApp/resources/localization"
            darwinLocalizableSourceFileName = "source_localizable.strings"
            darwinExport = true

            generatedReportPath = "$buildDir/"
            generatedReportFileName = "report.xlsx"
            reportExport = true

            generatedResClassesPath = generatedResFilesPath
            generatedResClassesPackage = generatedResPackage
        }
        .doTask()
}

tasks.named("build") {
    finalizedBy("buildResProcessor")
}

sourceSets.getByName("main") {
    java.srcDir("src/main/kotlin")
    java.srcDir(generatedResFilesPath)
}