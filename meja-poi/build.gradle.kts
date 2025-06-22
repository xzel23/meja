description = "Meja spreadsheet library - Apache POI implementation"

dependencies {
    implementation(project(":meja-core"))
    implementation(rootProject.libs.dua3.utility)

    // Apache POI
    implementation(rootProject.libs.poi) {
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }
    implementation(rootProject.libs.poi.ooxml) {
        // SVG support
        exclude(group = "org.apache.xmlgraphics", module = "batik-all")
        exclude(group = "org.apache.xmlgraphics", module = "xmlgraphics-commons")
        exclude(group = "xml-apis", module = "xml-apis-ext")
        // PDF support
        exclude(group = "org.apache.pdfbox", module = "pdfbox")
        exclude(group = "org.apache.pdfbox", module = "fontbox")
        exclude(group = "de.rototor.pdfbox", module = "graphics2d")
        // Exclude JavaFX dependencies to avoid conflicts with the JavaFX plugin
        exclude(group = "org.openjfx")
    }

    constraints {
        implementation(libs.commons.compress) {
            because("CVE-2024-26308, CVE-2024-25710")
        }
    }
}
