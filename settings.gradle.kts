import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

// Enable Foojay Toolchain resolver
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// define project name and version
rootProject.name = "dua3-meja"
val projectVersion = "8.2.0-SNAPSHOT"

// define subprojects
include("meja")
include("meja-generic")
include("meja-poi")
include("meja-ui")
include("meja-swing")
include("meja-db")
include("meja-samples")
include("meja-fx")
include("meja-samples-fx")

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = projectVersion.toDefaultLowerCase().contains("-rc")

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.52.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.2.0")
            plugin("cabe", "com.dua3.cabe").version("3.1.0")
            plugin("forbiddenapis", "de.thetaphi.forbiddenapis").version("3.9")
            plugin("sonar", "org.sonarqube").version("6.2.0.5505")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.1.0")

            version("dua3-utility", "20.0.0-SNAPSHOT")
            version("javafx", "23.0.2")
            version("dua3-fx", "1.5.0-SNAPSHOT")
            version("ikonli", "12.4.0")
            version("jspecify", "1.0.0")
            version("log4j-bom", "2.24.3")
            version("poi", "5.4.1")

            library("dua3-fx-application", "com.dua3.fx", "fx-application").versionRef("dua3-fx")
            library("dua3-utility", "com.dua3.utility", "utility").versionRef("dua3-utility")
            library("dua3-utility-db", "com.dua3.utility", "utility-db").versionRef("dua3-utility")
            library("dua3-utility-logging", "com.dua3.utility", "utility-logging").versionRef("dua3-utility")
            library("dua3-utility-logging-log4j", "com.dua3.utility", "utility-logging-log4j").versionRef("dua3-utility")
            library("dua3-utility-swing", "com.dua3.utility", "utility-swing").versionRef("dua3-utility")
            library("dua3-utility-fx", "com.dua3.utility", "utility-fx").versionRef("dua3-utility")
            library("dua3-utility-fx-controls", "com.dua3.utility", "utility-fx-controls").versionRef("dua3-utility")
            library("dua3-utility-fx-icons", "com.dua3.utility", "utility-fx-icons").versionRef("dua3-utility")
            library("dua3-utility-fx-icons-ikonli", "com.dua3.utility", "utility-fx-icons-ikonli").versionRef("dua3-utility")
            library("ikonli-fontawesome", "org.kordamp.ikonli", "ikonli-fontawesome-pack").versionRef("ikonli")
            library("ikonli-feather", "org.kordamp.ikonli", "ikonli-feather-pack").versionRef("ikonli")
            library("ikonli-javafx", "org.kordamp.ikonli", "ikonli-javafx").versionRef("ikonli")
            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")
            library("log4j-bom", "org.apache.logging.log4j", "log4j-bom").versionRef("log4j-bom")
            library("log4j-api", "org.apache.logging.log4j", "log4j-api").withoutVersion()
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").withoutVersion()
            library("log4j-jul", "org.apache.logging.log4j", "log4j-jul").withoutVersion()
            library("log4j-jcl", "org.apache.logging.log4j", "log4j-jcl").withoutVersion()
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").withoutVersion()
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").withoutVersion()
            library("poi", "org.apache.poi", "poi").versionRef("poi")
            library("poi-ooxml", "org.apache.poi", "poi-ooxml").versionRef("poi")

            // version overrides for libraries

            // use a newer version of commons-compress because of CVE-2024-26308, CVE-2024-25710
            library("commons-compress", "org.apache.commons", "commons-compress").version("1.26.2")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // Maven Central Repository
        mavenCentral()
        mavenLocal()

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
            println("snapshot version detected, adding local and snapshot Maven repositories")
            mavenLocal()

            // Sonatype Snapshots
            maven {
                name = "oss.sonatype.org-snapshots"
                url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                mavenContent {
                    snapshotsOnly()
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

        if (isReleaseCandidate || isSnapshot) {
            println("release candidate version detected, adding local and staging Maven repositories")
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
        }
    }

}
