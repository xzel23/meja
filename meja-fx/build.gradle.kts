plugins {
    id("org.openjfx.javafxplugin") version "0.0.13"
}

description = "Meja spreadsheet library - JavaFX tools"

javafx {
    version = rootProject.extra["javafxVersion"] as String
    modules = listOf( "javafx.base", "javafx.graphics", "javafx.controls" )
    configuration = "compileOnly"
}

dependencies {
    api(project(":meja"))
    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)
}
