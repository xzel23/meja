plugins {
    id("application")
}

description = "Meja spreadsheet library - samples (JavaFX)"

val javaToolVersion = JavaLanguageVersion.of(21)
val javaCompatibility = JavaVersion.VERSION_17

java {
    toolchain { languageVersion.set(javaToolVersion) }

    // JavaFX 21 is needed, but it is compatible with Java 17, so use Java 17 as language setting
    sourceCompatibility = javaCompatibility
    targetCompatibility = javaCompatibility
}

fun JavaExec.useToolchain() {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(javaToolVersion)
    })
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
    implementation(rootProject.libs.dua3.utility.fx)
    implementation(rootProject.libs.dua3.utility.fx.controls)

    implementation(rootProject.libs.log4j.core)
}

val runExcelViewer = task<JavaExec>("runExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
    useToolchain()
}

val runKitchenSink = task<JavaExec>("runKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
    useToolchain()
}
/*
val runTableModel = task<JavaExec>("runTableModel") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.TableModelDemo")
    enableAssertions = true
}

val runShowTestXlsx = task<JavaExec>("runShowTestXlsx") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.ShowTestXlsx")
    enableAssertions = true
}
*/
val runShowTestXlsx = task<JavaExec>("runFxShowTestXlsx") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
    useToolchain()
}

val runShowTestXlsxNoEA = task<JavaExec>("runFxShowTestXlsx (no assertions)") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = false
    useToolchain()
}
