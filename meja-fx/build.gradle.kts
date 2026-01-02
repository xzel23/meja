project.description = "Meja spreadsheet library - JavaFX tools"

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":meja-core"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    api(rootProject.libs.dua3.utility.fx) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
}
