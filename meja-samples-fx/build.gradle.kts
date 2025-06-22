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
    implementation(project(":meja-core"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-poi"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-fx"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.logging)
    implementation(rootProject.libs.dua3.utility.logging.log4j)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.dua3.utility.fx)
    implementation(rootProject.libs.dua3.utility.fx.controls)
    implementation(rootProject.libs.dua3.utility.fx.icons.ikonli)
    implementation(rootProject.libs.dua3.fx.application)

    // ikonli is used by fx-icons-ikonli; since there are dozens of icon packs, we must specify the ones to include here
    runtimeOnly(libs.ikonli.feather)
    runtimeOnly(rootProject.libs.log4j.jcl)
    runtimeOnly(rootProject.libs.log4j.jul)
}

tasks.register<JavaExec>("runFxExcelViewer") {
    description = "Run the FxExcelViewer sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxKitchenSink") {
    description = "Run the FxKitchenSink sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxShowTestXlsx") {
    description = "Run the FxShowTestXlsx sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
}
