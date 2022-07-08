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
