description = "Meja spreadsheet library - JavaFX tools"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api(project(":meja"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    api(rootProject.libs.dua3.utility.fx)
}
