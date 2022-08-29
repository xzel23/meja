plugins {
    id("application")
}

description = "Meja spreadsheet library - samples"

dependencies {
    implementation(project(":meja"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-swing"))
    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)
    implementation(group = "com.dua3.utility", name = "utility-swing", version = rootProject.extra["dua3UtilityVersion"] as String)

    // include utility-logging as implementation for SLF4J
    implementation(group = "com.dua3.utility", name = "utility-logging", version = rootProject.extra["dua3UtilityVersion"] as String)
    // route Log4J2 to SLF4J
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.18.0")

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
