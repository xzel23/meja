plugins {
    id("application")
}

description = "Meja spreadsheet library - samples (JavaFX)"

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
}

val runKitchenSink = task<JavaExec>("runKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
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
}

val runShowTestXlsxNoEA = task<JavaExec>("runFxShowTestXlsx (no assertions)") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = false
}
