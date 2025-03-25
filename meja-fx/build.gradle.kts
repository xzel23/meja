description = "Meja spreadsheet library - JavaFX tools"

java {
    version = JavaVersion.VERSION_21.toString()

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":meja"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    api(rootProject.libs.dua3.utility.fx)
}
