project.description = "Bill of Materials (BOM) for meja libraries"

plugins {
    id("java-platform")
    id("maven-publish")
}

dependencies {
    constraints {
        // Define constraints for all meja modules
        api("com.dua3.meja:meja-core:${project.version}")
        api("com.dua3.meja:meja-db:${project.version}")
        api("com.dua3.meja:meja-fx:${project.version}")
        api("com.dua3.meja:meja-generic:${project.version}")
        api("com.dua3.meja:meja-poi:${project.version}")
        api("com.dua3.meja:meja-swing:${project.version}")
        api("com.dua3.meja:meja-ui:${project.version}")

        // External dependencies used by utility modules
        // Common dependencies
        api(rootProject.libs.jspecify)

        // Logging dependencies
        api(rootProject.libs.log4j.api)

        // dua3 utility
        api(rootProject.libs.dua3.utility)
        api(rootProject.libs.dua3.utility.db)
        api(rootProject.libs.dua3.utility.logging)
        api(rootProject.libs.dua3.utility.logging.log4j)
        api(rootProject.libs.dua3.utility.logging.slf4j)
        api(rootProject.libs.dua3.utility.swing)
        api(rootProject.libs.dua3.utility.fx)
        api(rootProject.libs.dua3.utility.fx.controls)
        api(rootProject.libs.dua3.utility.fx.icons)
        api(rootProject.libs.dua3.utility.fx.icons.ikonli)

        // Apache POI
        api(rootProject.libs.poi)
        api(rootProject.libs.poi.ooxml)

        // Ikonli (used by FX icon module and samples)
        api(rootProject.libs.ikonli.javafx)
        api(rootProject.libs.ikonli.fontawesome)
        api(rootProject.libs.ikonli.feather)

        // dua3 FX application helper (used in samples-fx)
        api(rootProject.libs.dua3.fx.application)
    }
}

// Configure publication for BOM
publishing {
    publications {
        create<MavenPublication>("bomPublication") {
            from(components["javaPlatform"])

            groupId = "com.dua3.meja"
            artifactId = "meja-bom"
            version = project.version.toString()

            pom {
                name.set("meja BOM")
                description.set("Bill of Materials (BOM) for meja libraries")
                url.set("https://github.com/xzel23/meja.git")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("axh")
                        name.set("Axel Howind")
                        email.set("axh@dua3.com")
                        organization.set("dua3")
                        organizationUrl.set("https://www.dua3.com")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/xzel23/meja.git")
                    developerConnection.set("scm:git:https://github.com/xzel23/meja.git")
                    url.set("https://github.com/xzel23/meja.git")
                }

                // Add inceptionYear
                withXml {
                    val root = asNode()
                    root.appendNode("inceptionYear", "2015")
                }
            }
        }
    }

    // Repositories are now configured in the root build.gradle.kts file
}

// Signing is now configured in the root build.gradle.kts file
