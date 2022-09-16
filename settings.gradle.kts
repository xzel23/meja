rootProject.name = "dua3-meja"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
  
        // Maven Central Repository
        mavenCentral()

        // Sonatype Releases
        maven {
            name = "oss.sonatype.org-releases"
            url  = java.net.URI("https://s01.oss.sonatype.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        // Sonatype Snapshots
        maven {
            name = "oss.sonatype.org-snapshots"
            url  = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }
        
        // Apache releases
        maven {
            name = "apache-releases"
            url  = java.net.URI("https://repository.apache.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        // Apache staging
        maven {
            name = "apache-staging"
            url  = java.net.URI("https://repository.apache.org/content/repositories/staging/")
            mavenContent {
                releasesOnly()
            }
        }

        // Apache snapshots
        maven {
            name = "apache-snapshots"
            url  = java.net.URI("https://repository.apache.org/content/repositories/snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }

    }
}

include("meja")
include("meja-generic") 
include("meja-poi")
include("meja-swing")
include("meja-fx")
include("meja-db") 
include("meja-samples")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("versions", "com.github.ben-manes.versions").version("0.42.0")
            plugin("test-logger", "com.adarshr.test-logger").version("3.2.0")
            plugin("spotbugs", "com.github.spotbugs").version("5.0.12")
            plugin("cabe", "com.dua3.cabe").version("1.0.0")

            version("cabe", "1.0.0")
            library("cabe-annotations", "com.dua3.cabe", "cabe-annotations").versionRef("cabe")

            version("dua3-utility", "10.2.1-SNAPSHOT")
            library("dua3-utility", "com.dua3.utility", "utility").versionRef("dua3-utility")
            library("dua3-utility-db", "com.dua3.utility", "utility-db").versionRef("dua3-utility")
            library("dua3-utility-logging", "com.dua3.utility", "utility-logging").versionRef("dua3-utility")
            library("dua3-utility-swing", "com.dua3.utility", "utility-swing").versionRef("dua3-utility")

            version("slf4j", "2.0.1")
            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            version("log4j", "2.18.0")
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").versionRef("log4j")

            version("junit", "5.9.0")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")

            version("javafx", "19")

            version("poi", "5.2.3")
            library("poi", "org.apache.poi", "poi").versionRef("poi")
            library("poi-ooxml", "org.apache.poi", "poi-ooxml").versionRef("poi")
        }
    }
}
