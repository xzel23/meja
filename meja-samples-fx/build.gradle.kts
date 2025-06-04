plugins {
    id("application")
    alias(libs.plugins.javafx)
}

description = "Meja spreadsheet library - samples (JavaFX)"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withJavadocJar()
    withSourcesJar()
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml")
}

dependencies {
    implementation(project(":meja"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-poi"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-fx"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.logging)
    implementation(rootProject.libs.dua3.utility.logging.log4j)
    implementation(rootProject.libs.dua3.utility.fx) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
    implementation(rootProject.libs.dua3.utility.fx.controls) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
    implementation(rootProject.libs.dua3.utility.fx.icons.ikonli) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
    implementation(rootProject.libs.dua3.fx.application) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }

    // ikonli is used by fx-icons-ikonli; since there are dozens of icon packs, we must specify the ones to include here
    runtimeOnly(libs.ikonli.feather)

    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.log4j.core)
    runtimeOnly(rootProject.libs.log4j.jcl)
    runtimeOnly(rootProject.libs.log4j.jul)
}

val runExcelViewer = tasks.register<JavaExec>("runFxExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
}

val runKitchenSink = tasks.register<JavaExec>("runFxKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
}

val runShowTestXlsx = tasks.register<JavaExec>("runFxShowTestXlsx") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
}
