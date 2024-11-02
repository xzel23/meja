description = "Meja spreadsheet library - JavaFX tools"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withJavadocJar()
    withSourcesJar()

    // JavaFX 21 is needed, but it is compatible with Java 17, so use Java 17 as language setting
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":meja"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    api(rootProject.libs.dua3.utility.fx)
}
