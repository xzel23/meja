project.description = "Meja spreadsheet library - JavaFX tools"

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":meja-core"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
    api(rootProject.libs.dua3.utility.fx) { exclude(group = "org.openjfx") }
    implementation(rootProject.libs.dua3.utility.fx.controls) { exclude(group = "org.openjfx") }
}
