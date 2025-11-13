import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

rootProject.name = "dua3-meja"
val projectVersion = "9.0.0-beta17-SNAPSHOT"

include("meja-bom")
include("meja-core")
include("meja-generic")
include("meja-poi")
include("meja-ui")
include("meja-swing")
include("meja-db")
include("meja-samples")
include("meja-fx")
include("meja-samples-fx")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = !isSnapshot && projectVersion.toDefaultLowerCase().contains("-rc")

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.53.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.4.4")
            plugin("cabe", "com.dua3.cabe").version("3.3.0")
            plugin("forbiddenapis", "de.thetaphi.forbiddenapis").version("3.10")
            plugin("sonar", "org.sonarqube").version("7.0.1.6134")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.1.0")
            plugin("jmh", "me.champeau.jmh").version("0.7.3")
            plugin("jreleaser", "org.jreleaser").version("1.21.0")

            version("dua3-utility", "20.0.0-rc-6")
            version("ikonli", "12.4.0")
            version("javafx", "23.0.2")
            version("jmh", "1.37")
            version("jspecify", "1.0.0")
            version("junit-bom", "6.0.1")
            version("log4j-bom", "2.25.2")
            version("poi", "5.5.0")
            version("spotbugs", "4.9.8")

            library("dua3-utility-bom", "com.dua3.utility", "utility-bom").versionRef("dua3-utility")
            library("dua3-utility", "com.dua3.utility", "utility").withoutVersion()
            library("dua3-utility-db", "com.dua3.utility", "utility-db").withoutVersion()
            library("dua3-utility-logging", "com.dua3.utility", "utility-logging").withoutVersion()
            library("dua3-utility-fx-icons", "com.dua3.utility", "utility-fx-icons").withoutVersion()
            library("dua3-utility-fx-icons-ikonli", "com.dua3.utility", "utility-fx-icons-ikonli").withoutVersion()
            library("dua3-utility-logging-log4j", "com.dua3.utility", "utility-logging-log4j").withoutVersion()
            library("dua3-utility-logging-slf4j", "com.dua3.utility", "utility-logging-slf4j").withoutVersion()
            library("dua3-utility-swing", "com.dua3.utility", "utility-swing").withoutVersion()
            library("dua3-utility-fx", "com.dua3.utility", "utility-fx").withoutVersion()
            library("dua3-utility-fx-controls", "com.dua3.utility", "utility-fx-controls").withoutVersion()
            library("ikonli-fontawesome", "org.kordamp.ikonli", "ikonli-fontawesome-pack").versionRef("ikonli")
            library("ikonli-feather", "org.kordamp.ikonli", "ikonli-feather-pack").versionRef("ikonli")
            library("ikonli-javafx", "org.kordamp.ikonli", "ikonli-javafx").versionRef("ikonli")
            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")
            library("junit-bom", "org.junit", "junit-bom").versionRef("junit-bom")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("junit-platform-launcher", "org.junit.platform", "junit-platform-launcher").withoutVersion()
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
            library("commons-compress", "org.apache.commons", "commons-compress").version("1.26.2")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // Maven Central Repository
        mavenCentral()

        // Sonatype Releases
        maven {
            name = "central.sonatype.com-releases"
            url = java.net.URI("https://oss.sonatype.org/content/repositories/releases/")
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
            println("snapshot version detected, adding Maven snapshot repositories")

            mavenLocal()

            // Sonatype Snapshots
            maven {
                name = "Central Portal Snapshots"
                url = java.net.URI("https://central.sonatype.com/repository/maven-snapshots/")
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

            // Apache staging
            maven {
                name = "apache-staging"
                url = java.net.URI( "https://repository.apache.org/content/repositories/staging/")
            }
        }

        if (isReleaseCandidate) {
            println("release candidate version detected, adding Maven staging repositories")

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
