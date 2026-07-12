import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

rootProject.name = "dua3-meja"

fun versionCatalogVersion(alias: String): String {
    val catalog = file("gradle/version.toml")
    val versions = catalog.readLines()
        .dropWhile { it.trim() != "[versions]" }
        .drop(1)
        .takeWhile { !it.trim().startsWith("[") }

    val versionDeclaration = Regex("""^\s*${Regex.escape(alias)}\s*=\s*"([^"]+)"\s*(?:#.*)?$""")
    return versions.firstNotNullOfOrNull { line ->
        versionDeclaration.matchEntire(line)?.groupValues?.get(1)
    } ?: throw GradleException("version '$alias' not found in ${catalog.path}")
}

val projectVersion = versionCatalogVersion("projectVersion")

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

gradle.projectsLoaded {
    rootProject.allprojects {
        version = projectVersion
    }
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = !isSnapshot && projectVersion.toDefaultLowerCase().contains("-rc")

    if (isSnapshot && !projectVersion.endsWith("-SNAPSHOT")) {
        throw GradleException("inconsistent version definition: $projectVersion does not end with SNAPSHOT")
    }

    versionCatalogs {
        create("libs") {
            from(files("gradle/version.toml"))
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
