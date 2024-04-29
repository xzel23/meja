// define project name and version
rootProject.name = "dua3-meja"
val projectVersion = "5.0-SNAPSHOT"

// define subprojects
include("meja")
include("meja-generic")
include("meja-poi")
include("meja-ui")
include("meja-swing")
//include("meja-fx")
include("meja-db")
include("meja-samples")

// use plugin to add JVM toolchain repository
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.endsWith("SNAPSHOT")

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.51.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.0.12")
            plugin("cabe", "com.dua3.cabe").version("2.1.2")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.1.0")

            version("cabe", "2.0")
            version("dua3-utility", "13.0-SNAPSHOT")
            version("javafx", "21.0.2")
            version("log4j", "2.23.1")
            version("poi", "5.2.5")
            version("controlsfx", "11.2.1")

            library("cabe-annotations", "com.dua3.cabe", "cabe-annotations").versionRef("cabe")
            library("dua3-utility", "com.dua3.utility", "utility").versionRef("dua3-utility")
            library("dua3-utility-db", "com.dua3.utility", "utility-db").versionRef("dua3-utility")
            library("dua3-utility-logging", "com.dua3.utility", "utility-logging").versionRef("dua3-utility")
            library("dua3-utility-swing", "com.dua3.utility", "utility-swing").versionRef("dua3-utility")
            library("dua3-utility-fx", "com.dua3.utility", "utility-fx").versionRef("dua3-utility")
            library("log4j-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("poi", "org.apache.poi", "poi").versionRef("poi")
            library("poi-ooxml", "org.apache.poi", "poi-ooxml").versionRef("poi")
            library("controlsfx", "org.controlsfx", "controlsfx").versionRef("controlsfx")

            // version overrides for libraries

            // use a newer version of commons-compress because of CVE-2024-26308, CVE-2024-25710
            library("commons-compress", "org.apache.commons", "commons-compress").version("1.26.1")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // Maven Central Repository
        mavenCentral()

        // Sonatype Releases
        maven {
            name = "oss.sonatype.org-releases"
            url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        // Apache releases
        maven {
            name = "apache-releases"
            url = java.net.URI("https://repository.apache.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        if (isSnapshot) {
            // local maven repository
            mavenLocal()

            // Sonatype Snapshots
            maven {
                name = "oss.sonatype.org-snapshots"
                url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                mavenContent {
                    snapshotsOnly()
                }
            }

            // Apache staging
            maven {
                name = "apache-staging"
                url = java.net.URI("https://repository.apache.org/content/repositories/staging/")
                mavenContent {
                    releasesOnly()
                }
            }

            // Apache snapshots
            maven {
                name = "apache-snapshots"
                url = java.net.URI("https://repository.apache.org/content/repositories/snapshots/")
                mavenContent {
                    snapshotsOnly()
                }
            }
        }
    }

}
