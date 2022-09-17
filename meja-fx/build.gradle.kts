plugins {
    alias(libs.plugins.javafx)
}

description = "Meja spreadsheet library - JavaFX tools"

javafx {
    version = rootProject.libs.versions.javafx.get()
    modules = listOf( "javafx.base", "javafx.graphics", "javafx.controls" )
    configuration = "compileOnly"
}

dependencies {
    api(project(":meja"))
    implementation(rootProject.libs.dua3.utility)
}
