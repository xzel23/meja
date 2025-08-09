// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.cabe.processor.Configuration
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("version-catalog")
    id("signing")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.forbiddenapis)
    alias(libs.plugins.javafx)
    alias(libs.plugins.jmh)
    alias(libs.plugins.sonar)
    alias(libs.plugins.jreleaser)
}

/////////////////////////////////////////////////////////////////////////////
// Meta data object
/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val DESCRIPTION = "Meja spreadsheet library."
    const val INCEPTION_YEAR = "2019"
    const val GROUP = "com.dua3.meja"
    const val SCM = "https://github.com/xzel23/meja.git"
    const val LICENSE_NAME = "The Apache Software License, Version 2.0"
    const val LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}

/////////////////////////////////////////////////////////////////////////////
// Root project configuration
/////////////////////////////////////////////////////////////////////////////

project.description = Meta.DESCRIPTION

dependencies {
    // Aggregate all subprojects for JaCoCo report aggregation
    jacocoAggregation(project(":meja-core"))
    jacocoAggregation(project(":meja-generic"))
    jacocoAggregation(project(":meja-poi"))
    jacocoAggregation(project(":meja-ui"))
    jacocoAggregation(project(":meja-fx"))
    jacocoAggregation(project(":meja-swing"))
    jacocoAggregation(project(":meja-db"))
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SonarQube root project config
sonar {
    properties {
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
        property("sonar.coverage.exclusions", "**/samples/**")
    }
}

/////////////////////////////////////////////////////////////////////////////
// Subprojects configuration
/////////////////////////////////////////////////////////////////////////////

subprojects {

    // Set project version from root libs.versions
    project.version = rootProject.libs.versions.projectVersion.get()

    fun isDevelopmentVersion(versionString: String): Boolean {
        val v = versionString.toDefaultLowerCase()
        val markers = listOf("snapshot", "alpha", "beta")
        return markers.any { marker -> v.contains("-$marker") || v.contains(".$marker") }
    }

    val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
    val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

    // Apply common plugins
    apply(plugin = "maven-publish")
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = rootProject.libs.plugins.javafx.get().pluginId)
    apply(plugin = rootProject.libs.plugins.versions.get().pluginId)
    apply(plugin = rootProject.libs.plugins.test.logger.get().pluginId)

    // Skip some plugins for BOM project
    if (!project.name.endsWith("-bom")) {
        apply(plugin = "jacoco")
        apply(plugin = "java-library")
        apply(plugin = "jvm-test-suite")
        apply(plugin = rootProject.libs.plugins.spotbugs.get().pluginId)
        apply(plugin = rootProject.libs.plugins.cabe.get().pluginId)
        apply(plugin = rootProject.libs.plugins.forbiddenapis.get().pluginId)
        apply(plugin = rootProject.libs.plugins.jmh.get().pluginId)
    }

    // Java configuration for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            targetCompatibility = JavaVersion.VERSION_21
            sourceCompatibility = targetCompatibility

            withJavadocJar()
            withSourcesJar()
        }

        cabe {
            if (isReleaseVersion) {
                config.set(Configuration.parse("publicApi=THROW_IAE:privateApi=ASSERT"))
            } else {
                config.set(Configuration.DEVELOPMENT)
            }
        }

        // JaCoCo
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(false)
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.jacocoTestReport)
        }
    }

    // SonarQube properties
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/samples/**")
        }
    }

    // Dependencies for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        dependencies {
            api(rootProject.libs.dua3.utility.fx)
            implementation(platform(rootProject.libs.dua3.utility.bom))
            implementation(rootProject.libs.jspecify)
            implementation(platform(rootProject.libs.log4j.bom))
            implementation(rootProject.libs.log4j.api)
        }

        idea {
            module {
                inheritOutputDirs = false
                outputDir = project.layout.buildDirectory.file("classes/java/main/").get().asFile
                testOutputDir = project.layout.buildDirectory.file("classes/java/test/").get().asFile
            }
        }

        testing {
            suites {
                val test by getting(JvmTestSuite::class) {
                    useJUnitJupiter()
                    dependencies {
                        implementation(rootProject.libs.log4j.core)
                    }
                    targets {
                        all {
                            testTask {
                                // enable assertions and use headless mode for AWT in unit tests
                                jvmArgs(
                                    "-ea",
                                    "-Djava.awt.headless=true",
                                    "-Dprism.order=sw",
                                    "-Dsun.java2d.d3d=false",
                                    "-Dsun.java2d.opengl=false",
                                    "-Dsun.java2d.pmoffscreen=false"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    // Java compilation and Javadoc config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module"))
            options.javaModuleVersion.set(provider { project.version as String })
            options.release.set(java.targetCompatibility.majorVersion.toInt())
        }
        tasks.compileTestJava {
            options.encoding = "UTF-8"
        }
        tasks.javadoc {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing/private")
            }
        }
    }

    // JMH config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        jmh {
            jmhVersion = rootProject.libs.versions.jmh
            warmupIterations = 2
            iterations = 5
            fork = 1
        }
    }

    // Forbidden APIs and SpotBugs for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        // === FORBIDDEN APIS ===
        tasks.withType(de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis::class).configureEach {
            bundledSignatures = setOf("jdk-internal", "jdk-deprecated")
            ignoreFailures = false
        }

        // === SPOTBUGS ===
        spotbugs {
            toolVersion.set(rootProject.libs.versions.spotbugs)
            excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    // Jar duplicates strategy for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    // --- PUBLISHING ---

    configure<PublishingExtension> {
        // Repositories for publishing
        repositories {
            // Sonatype snapshots for snapshot versions
            if (isSnapshot) {
                maven {
                    name = "sonatypeSnapshots"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                    credentials {
                        username = System.getenv("SONATYPE_USERNAME")
                        password = System.getenv("SONATYPE_PASSWORD")
                    }
                }
            }

            // Always add root-level staging directory for JReleaser
            maven {
                name = "stagingDirectory"
                url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            }
        }

        // Publications for non-BOM projects
        if (!project.name.endsWith("-bom")) {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = Meta.GROUP
                    artifactId = project.name
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        url.set(Meta.SCM)

                        licenses {
                            license {
                                name.set(Meta.LICENSE_NAME)
                                url.set(Meta.LICENSE_URL)
                            }
                        }

                        developers {
                            developer {
                                id.set(Meta.DEVELOPER_ID)
                                name.set(Meta.DEVELOPER_NAME)
                                email.set(Meta.DEVELOPER_EMAIL)
                                organization.set(Meta.ORGANIZATION_NAME)
                                organizationUrl.set(Meta.ORGANIZATION_URL)
                            }
                        }

                        scm {
                            connection.set("scm:git:${Meta.SCM}")
                            developerConnection.set("scm:git:${Meta.SCM}")
                            url.set(Meta.SCM)
                        }

                        withXml {
                            val root = asNode()
                            root.appendNode("inceptionYear", "2019")
                        }
                    }
                }
            }
        }
    }

    // Task to publish to staging directory per subproject
    val publishToStagingDirectory by tasks.registering {
        group = "publishing"
        description = "Publish artifacts to root staging directory for JReleaser"

        dependsOn(tasks.withType<PublishToMavenRepository>().matching {
            it.repository.name == "stagingDirectory"
        })
    }

    // Signing configuration deferred until after evaluation
    afterEvaluate {
        configure<SigningExtension> {
            val shouldSign = !project.version.toString().lowercase().contains("snapshot")
            setRequired(shouldSign && gradle.taskGraph.hasTask("publish"))

            val publishing = project.extensions.getByType<PublishingExtension>()

            if (project.name.endsWith("-bom")) {
                if (publishing.publications.names.contains("bomPublication")) {
                    sign(publishing.publications["bomPublication"])
                }
            } else {
                if (publishing.publications.names.contains("mavenJava")) {
                    sign(publishing.publications["mavenJava"])
                }
            }
        }
    }

    // set the project description after evaluation because it is not yet visible when the POM is first created
    afterEvaluate {
        project.extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                pom {
                    if (description.orNull.isNullOrBlank()) {
                        description.set(project.description ?: "No description provided")
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Root project tasks and JReleaser configuration
/////////////////////////////////////////////////////////////////////////////

// Aggregate all subprojects' publishToStagingDirectory tasks into a root-level task
tasks.register("publishToStagingDirectory") {
    group = "publishing"
    description = "Publish all subprojects' artifacts to root staging directory for JReleaser"

    dependsOn(subprojects.mapNotNull { it.tasks.findByName("publishToStagingDirectory") })
}

// Make jreleaserDeploy depend on the root-level publishToStagingDirectory task
tasks.named("jreleaserDeploy") {
    dependsOn("publishToStagingDirectory")
}

jreleaser {
    project {
        name.set(rootProject.name)
        version.set(rootProject.libs.versions.projectVersion.get())
        group = Meta.GROUP
        authors.set(listOf(Meta.DEVELOPER_NAME))
        license.set(Meta.LICENSE_NAME)
        links {
            homepage.set(Meta.ORGANIZATION_URL)
        }
        inceptionYear.set(Meta.INCEPTION_YEAR)
        gitRootSearch.set(true)
    }

    signing {
        publicKey.set(System.getenv("SIGNING_PUBLIC_KEY"))
        secretKey.set(System.getenv("SIGNING_SECRET_KEY"))
        passphrase.set(System.getenv("SIGNING_PASSWORD"))
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }

    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("build/staging-deploy")
                    username.set(System.getenv("SONATYPE_USERNAME"))
                    password.set(System.getenv("SONATYPE_PASSWORD"))
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                    applyMavenCentralRules.set(true)
                    verifyPom.set(false)
                    snapshotSupported.set(true)
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                    username.set(System.getenv("SONATYPE_USERNAME"))
                    password.set(System.getenv("SONATYPE_PASSWORD"))
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Utility tasks
/////////////////////////////////////////////////////////////////////////////

// Task to generate JReleaser configuration file for reference
tasks.register("generateJReleaserConfig") {
    description = "Generates JReleaser configuration file for reference"
    group = "documentation"

    doLast {
        val process = ProcessBuilder("./gradlew", "jreleaserConfig", "-PconfigFile=jreleaser-config.yml")
            .directory(project.rootDir)
            .inheritIO()
            .start()
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("JReleaser configuration file generated at: jreleaser-config.yml")
        } else {
            println("Failed to generate JReleaser configuration file. Exit code: $exitCode")
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Versions plugin configuration for all projects
/////////////////////////////////////////////////////////////////////////////

allprojects {
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|alpha|beta|b|M)(-?[0-9]*)?".toRegex()
        return stableKeyword || !regex.matches(version)
    }

    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            !isStable(candidate.version)
        }
    }
}
