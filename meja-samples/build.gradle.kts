plugins {
    id("application")
}

project.description = "Meja spreadsheet library - samples"

dependencies {
    implementation(project(":meja-core"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-swing"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.logging)
    implementation(rootProject.libs.dua3.utility.logging.log4j)
    implementation(rootProject.libs.dua3.utility.swing)

    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slb4j)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.jcl)

    implementation(project(":meja-poi"))
}

tasks.register<JavaExec>("runExcelViewer") {
    description = "Run the SwingExcelViewer sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.excelviewer.SwingExcelViewer")
    enableAssertions = true
}

tasks.register<JavaExec>("runKitchenSink") {
    description = "Run the KitchenSink sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.KitchenSink")
    enableAssertions = true
    systemProperty("logwindow", "1")
}

tasks.register<JavaExec>("runTableModel") {
    description = "Run the TableModelDemo sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.TableModelDemo")
    enableAssertions = true
}

tasks.register<JavaExec>("runShowTestXlsx") {
    description = "Run the ShowTestXlsx sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.samples.ShowTestXlsx")
    enableAssertions = true
}
