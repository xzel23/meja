plugins {
    id("application")
}

project.description = "Meja spreadsheet library - samples (JavaFX)"

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":meja-core"))
    implementation(project(":meja-generic"))
    implementation(project(":meja-poi"))
    implementation(project(":meja-ui"))
    implementation(project(":meja-fx"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slb4j)
    implementation(rootProject.libs.dua3.utility.fx)
    implementation(rootProject.libs.dua3.utility.fx.controls)
    implementation(rootProject.libs.dua3.utility.fx.icons.ikonli)
    implementation(rootProject.libs.ikonli.feather)
}

tasks.register<JavaExec>("runFxExcelViewer") {
    description = "Run the FxExcelViewer sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxKitchenSink") {
    description = "Run the FxKitchenSink sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxShowTestXlsx") {
    description = "Run the FxShowTestXlsx sample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
}
