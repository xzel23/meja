description = "Meja spreadsheet library - database tools"

dependencies {
    api(project(":meja"))
    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)
}
