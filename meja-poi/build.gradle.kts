description = "Meja spreadsheet library - Apache POI implementation"

dependencies {
    implementation(project(":meja"))
    implementation(rootProject.libs.dua3.utility)

    // Apache POI
    implementation(rootProject.libs.poi)
    implementation(rootProject.libs.poi.ooxml) {
        // SVG support
        exclude(group = "org.apache.xmlgraphics", module = "batik-all")
        exclude(group = "org.apache.xmlgraphics", module = "xmlgraphics-commons")
        exclude(group = "xml-apis", module = "xml-apis-ext")
        // PDF support
        exclude(group = "org.apache.pdfbox", module = "pdfbox")
        exclude(group = "org.apache.pdfbox", module = "fontbox")
        exclude(group = "de.rototor.pdfbox", module = "graphics2d")
    }
}
