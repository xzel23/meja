description = "Meja spreadsheet library - ui base module"

dependencies {
    api(project(":meja"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.logging)
    implementation(rootProject.libs.dua3.utility.logging.log4j)
}
