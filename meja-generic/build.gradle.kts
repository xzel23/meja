description = "Meja spreadsheet library - generic implementation"

dependencies {
    implementation(project(":meja"))
    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)
}
