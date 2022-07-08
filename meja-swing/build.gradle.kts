description = "Meja spreadsheet library - swing tools"

dependencies {
    implementation(project(":meja"))

    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)
    implementation(group = "com.dua3.utility", name = "utility-swing", version = rootProject.extra["dua3UtilityVersion"] as String)
}
