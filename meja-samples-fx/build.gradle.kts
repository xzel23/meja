plugins {
    id("application")
}

description = "Meja spreadsheet library - samples (JavaFX)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility
    version = targetCompatibility.toString()

    withJavadocJar()
    withSourcesJar()
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

val runExcelViewer = tasks.register<JavaExec>("runFxExcelViewer") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxExcelViewer")
    enableAssertions = true
}

val runKitchenSink = tasks.register<JavaExec>("runFxKitchenSink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxKitchenSink")
    enableAssertions = true
}

val runShowTestXlsx = tasks.register<JavaExec>("runFxShowTestXlsx") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.meja.fx.samples.FxShowTestXlsx")
    enableAssertions = true
}
