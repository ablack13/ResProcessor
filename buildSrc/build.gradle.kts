import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.11.0")
    implementation("org.apache.poi:poi:5.2.2")

}