plugins {
    id("application")
    alias(libs.plugins.javafx)
}

description = "Meja spreadsheet library - samples (JavaFX)"

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "implementation"
    modules = listOf("javafx.base", "javafx.graphics", "javafx.controls")
}

dependencies {
    implementation(project(":meja"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-fx"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.fx)

    implementation(rootProject.libs.log4j.core)
}
/*
val runExcelViewer = task<JavaExec>("runExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.excelviewer.SwingExcelViewer")
    enableAssertions = true
}
*/
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