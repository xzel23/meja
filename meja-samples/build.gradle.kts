plugins {
    id("application")
}

description = "Meja spreadsheet library - samples"

dependencies {
    implementation(project(":meja"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-swing"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.logging)
    implementation(rootProject.libs.dua3.utility.logging.log4j)
    implementation(rootProject.libs.dua3.utility.swing)

    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.jcl)

    implementation(project(":meja-poi"))
}

val runExcelViewer = task<JavaExec>("runExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.excelviewer.SwingExcelViewer")
    enableAssertions = true
}

val runKitchenSink = task<JavaExec>("runKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.KitchenSink")
    enableAssertions = true
    systemProperty("logwindow", "1")
}

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
