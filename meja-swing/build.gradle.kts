description = "Meja spreadsheet library - swing tools"

dependencies {
    api(project(":meja"))
    api(project(":meja-ui"))

    implementation(rootProject.libs.dua3.utility)
    implementation(rootProject.libs.dua3.utility.swing)
}
