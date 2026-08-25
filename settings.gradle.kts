import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

private val publishableModuleNames = listOf(
    "meja-core",
    "meja-db",
    "meja-fx",
    "meja-generic",
    "meja-poi",
    "meja-swing",
    "meja-ui"
)

private data class ReleaseVersions(
    val bomVersion: String,
    val moduleVersions: Map<String, String>,
    val selectedModules: Set<String>
)

private fun readReleaseVersions(file: File, requireSelection: Boolean): ReleaseVersions {
    require(file.isFile) { "release file does not exist: ${file.path}" }
    var section = ""
    val values = mutableMapOf<String, MutableMap<String, String>>()
    val tablePattern = Regex("""^\[([A-Za-z0-9_.-]+)]$""")
    val valuePattern = Regex("""^([A-Za-z][A-Za-z0-9_-]*)\s*=\s*(.+)$""")
    file.forEachLine { rawLine ->
        val line = rawLine.substringBefore('#').trim()
        if (line.isEmpty()) return@forEachLine
        tablePattern.matchEntire(line)?.let {
            section = it.groupValues[1]
            values.getOrPut(section) { mutableMapOf() }
            return@forEachLine
        }
        valuePattern.matchEntire(line)?.let {
            require(section.isNotEmpty()) { "value outside a TOML table in ${file.path}: $line" }
            values.getOrPut(section) { mutableMapOf() }[it.groupValues[1]] = it.groupValues[2].trim().removeSurrounding("\"")
            return@forEachLine
        }
        throw GradleException("unsupported release TOML syntax in ${file.path}: $line")
    }
    val release = values["release"] ?: throw GradleException("[release] table missing from ${file.path}")
    val bomVersion = release["bomVersion"] ?: throw GradleException("release.bomVersion missing from ${file.path}")
    val moduleVersions = publishableModuleNames.associateWith { name ->
        values["modules.$name"]?.get("version") ?: throw GradleException("modules.$name.version missing from ${file.path}")
    }
    val selectedModules = if (requireSelection) publishableModuleNames.filter { name ->
        values["modules.$name"]?.get("selected")?.toBooleanStrictOrNull()
            ?: throw GradleException("modules.$name.selected missing or invalid in ${file.path}")
    }.toSet() else emptySet()
    return ReleaseVersions(bomVersion, moduleVersions, selectedModules)
}

plugins {
    id("io.github.ben-manes.versions.settings") version "0.61.0"
}

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

private val developmentVersion = versionCatalogVersion("projectVersion")
private val releaseStateFile = file("gradle/release-state.toml")
private val publishedRelease = readReleaseVersions(releaseStateFile, requireSelection = false)
private val preparedReleasePlanFile = file("gradle/prepared-release.toml")
private val preparedRelease = preparedReleasePlanFile.takeIf(File::isFile)?.let { readReleaseVersions(it, requireSelection = true) }
if (preparedRelease != null && developmentVersion != preparedRelease.bomVersion) {
    throw GradleException(
        "prepared release ${preparedRelease.bomVersion} requires gradle/version.toml projectVersion " +
            "to be ${preparedRelease.bomVersion}, but it is $developmentVersion"
    )
}
private val effectiveBomVersion = preparedRelease?.bomVersion ?: developmentVersion
private val effectiveModuleVersions = preparedRelease?.moduleVersions ?: publishableModuleNames.associateWith { developmentVersion }
private val selectedReleaseModules = preparedRelease?.selectedModules ?: emptySet()

gradle.extra["releaseStateFile"] = releaseStateFile
gradle.extra["publishedReleaseBomVersion"] = publishedRelease.bomVersion
gradle.extra["publishedReleaseModuleVersions"] = publishedRelease.moduleVersions
gradle.extra["preparedReleasePlanFile"] = preparedReleasePlanFile
gradle.extra["releaseBomVersion"] = effectiveBomVersion
gradle.extra["releaseModuleVersions"] = effectiveModuleVersions
gradle.extra["releasePlanPresent"] = (preparedRelease != null)
gradle.extra["releaseSelectedModules"] = selectedReleaseModules

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
        version = effectiveModuleVersions[name] ?: effectiveBomVersion
    }
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = effectiveBomVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = !isSnapshot && effectiveBomVersion.toDefaultLowerCase().contains("-rc")

    if (isSnapshot && !effectiveBomVersion.endsWith("-SNAPSHOT")) {
        throw GradleException("inconsistent version definition: $effectiveBomVersion does not end with SNAPSHOT")
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
