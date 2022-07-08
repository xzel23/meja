description = "Meja spreadsheet library - Apache POI implementation"

dependencies {
    implementation(project(":meja"))
    implementation(group = "com.dua3.utility", name = "utility", version = rootProject.extra["dua3UtilityVersion"] as String)

    // Apache POI
    val poiVersion = "5.2.2"
    implementation(group = "org.apache.poi", name = "poi", version = poiVersion)
    implementation(group = "org.apache.poi", name = "poi-ooxml", version = poiVersion) {
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
