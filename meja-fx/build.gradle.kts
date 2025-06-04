description = "Meja spreadsheet library - JavaFX tools"

plugins {
    alias(libs.plugins.javafx)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withJavadocJar()
    withSourcesJar()
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    api(project(":meja"))
    api(project(":meja-ui"))
    implementation(rootProject.libs.dua3.utility)
    api(rootProject.libs.dua3.utility.fx) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
}
