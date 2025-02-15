plugins {
    id("application")
}

description = "Meja spreadsheet library - samples (JavaFX)"

val javaToolVersion = JavaLanguageVersion.of(21)
val javaCompatibility = JavaVersion.VERSION_21
val javaRuntimeVersion = "EA"

java {
    toolchain { languageVersion.set(javaToolVersion) }

    // Java 17 is used for everything but the JavaFX related modules
    sourceCompatibility = javaCompatibility
    targetCompatibility = javaCompatibility
}

fun JavaExec.useToolchain() {
    if (javaRuntimeVersion.toString() == "EA") {
        val jdkPreviewBase = file("${System.getProperty("user.home")}/bin/jdk-preview").absolutePath
        val javaHome = "${jdkPreviewBase}/jdk/Contents/Home"
        val pathToFx = "${jdkPreviewBase}/javafx-sdk/lib"
        val javafxModules = listOf("javafx.controls", "javafx.fxml")

        environment("PATH_TO_FX", pathToFx)
        environment("JAVA_HOME", javaHome)
        environment("JAVA_TOOL_OPTIONS", "--module-path=$pathToFx")

        jvmArgs = listOf(
            "--module-path", pathToFx, // Explicit module path for JavaFX
            "--add-modules", javafxModules.joinToString(",") // Adds required JavaFX modules
        )

        executable = "${javaHome}/bin/java"

    } else {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(javaToolVersion)
        })
    }
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

val runExcelViewer = task<JavaExec>("runFxExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
    useToolchain()
}

val runKitchenSink = task<JavaExec>("runFxKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
    useToolchain()
}

val runShowTestXlsx = task<JavaExec>("runFxShowTestXlsx") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
    useToolchain()
}
