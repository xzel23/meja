plugins {
    id("application")
}

description = "Meja spreadsheet library - samples"

dependencies {
    implementation(project(":meja"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-swing"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.swing)

    // include utility-logging as implementation for SLF4J
    implementation(rootProject.libs.dua3.utility.logging)
    // route Log4J2 to SLF4J
    implementation(rootProject.libs.log4j.to.slf4j)

    runtimeOnly(project(":meja-poi"))
}

val runExcelViewer = task<JavaExec>("runExcelViewer") {
    dependsOn("classes")
    mainClass.set("com.dua3.meja.excelviewer.SwingExcelViewer")
    enableAssertions = true
}

val runKitchenSink = task<JavaExec>("runKitchenSink") {
    dependsOn("classes")
    mainClass.set("com.dua3.meja.samples.KitchenSink")
    enableAssertions = true
}
