plugins {
    id("application")
}

description = "Meja spreadsheet library - samples (JavaFX)"

val javaToolVersion = JavaLanguageVersion.of(21)
val javaCompatibility = JavaVersion.VERSION_21

java {
    toolchain { languageVersion.set(javaToolVersion) }

    // Java 17 is used for everything but the JavaFX related modules
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
    implementation(rootProject.libs.dua3.utility.fx.icons.ikonli)
    implementation(rootProject.libs.dua3.fx.application)

    // ikonli is used by fx-icons-ikonli; since there are dozens of icon packs, we must specify the ones to include here
    runtimeOnly(libs.ikonli.feather)

    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.log4j.core)
    runtimeOnly(rootProject.libs.log4j.jcl)
    runtimeOnly(rootProject.libs.log4j.jul)
}

fun JavaExec.useJDKPreviewToolchain() {
    val javaHome = file("/Users/axelhowind/bin/jdk-preview/jdk/Contents/Home")
    val pathToFx = "/Users/axelhowind/bin/jdk-preview/javafx-sdk/lib"
    val javaToolOptions = "--module-path=$pathToFx --add-modules javafx.controls,javafx.fxml"
    val javafxModules = listOf("javafx.controls", "javafx.fxml")

    environment("PATH_TO_FX", pathToFx)               // Make available to the runtime
    environment("JAVA_HOME", "/Users/axelhowind/bin/jdk-preview/jdk/Contents/Home") // Use JDK 24 location
    environment("JAVA_TOOL_OPTIONS", "--module-path=$pathToFx") // Ensure module path is set

    jvmArgs = listOf(
        "--module-path", pathToFx, // Explicit module path for JavaFX
        "--add-modules", javafxModules.joinToString(",") // Adds required JavaFX modules
    )

    executable = file("${javaHome.absolutePath}/bin/java").absolutePath
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

val runShowTestXlsxJDKPreview = task<JavaExec>("runShowTestXlsxJDKPreview") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
    useJDKPreviewToolchain()
}

val runShowTestXlsxNoEA = task<JavaExec>("runFxShowTestXlsx (no assertions)") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = false
    useToolchain()
}
