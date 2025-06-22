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

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import java.net.URI

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("maven-publish")
    id("signing")
    id("idea")
    id("jacoco")
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.forbiddenapis)
    alias(libs.plugins.sonar)
}

/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val GROUP = "com.dua3.meja"
    const val SCM = "https://github.com/xzel23/meja.git"
    const val REPO = "public"
    const val LICENSE_NAME = "The Apache Software License, Version 2.0"
    const val LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}
/////////////////////////////////////////////////////////////////////////////

subprojects {

    project.version = rootProject.libs.versions.projectVersion.get()

    fun isDevelopmentVersion(versionString : String) : Boolean {
        val v = versionString.toDefaultLowerCase()
        val markers = listOf("snapshot", "alpha", "beta")
        for (marker in markers) {
            if (v.contains("-$marker") || v.contains(".$marker")) {
                return true
            }
        }
        return false
    }
    val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
    val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

    apply(plugin = "java-library")
    apply(plugin = "jvm-test-suite")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "com.github.spotbugs")
    // Temporarily disable Cabe processor to avoid JavaFX module conflicts
    apply(plugin = "com.dua3.cabe")
    apply(plugin = "de.thetaphi.forbiddenapis")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        withJavadocJar()
        withSourcesJar()
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:-module")
        options.javaModuleVersion.set(provider { project.version as String })
        options.release.set(java.targetCompatibility.majorVersion.toInt())
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
    }

    // Cabe
    cabe {
        if (isReleaseVersion) {
            config.set(com.dua3.cabe.processor.Configuration.STANDARD)
        } else {
            config.set(com.dua3.cabe.processor.Configuration.DEVELOPMENT)
        }
    }

    // JaCoCo
    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(false)
        }
    }

    // Configure test task to use JaCoCo
    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    // Sonar
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
        }
    }

    // dependencies
    dependencies {
        // Cabe (source annotations)
        implementation(rootProject.libs.jspecify)

        // LOG4J
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
                            // Use headless mode for AWT in unit tests
                            jvmArgs("-ea", "-Djava.awt.headless=true")
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.STANDARD
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
    }

    // === publication: MAVEN = == >

    // Create the publication with the pom configuration:
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = Meta.GROUP
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])

                pom {
                    withXml {
                        val root = asNode()
                        root.appendNode("description", project.description)
                        root.appendNode("name", project.name)
                        root.appendNode("url", Meta.SCM)
                    }

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
                        url.set(Meta.SCM)
                    }
                }
            }
        }

        repositories {
            // Sonatype OSSRH
            maven {
                val releaseRepo = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (isSnapshot) snapshotRepo else releaseRepo
                credentials {
                    username = project.properties["ossrhUsername"].toString()
                    password = project.properties["ossrhPassword"].toString()
                }
            }
        }
    }

    // === sign artifacts
    signing {
        isRequired = isReleaseVersion && gradle.taskGraph.hasTask("publish")
        sign(publishing.publications["maven"])
    }

    // === FORBIDDEN APIS ===
    forbiddenApis {
        bundledSignatures = setOf("jdk-internal", "jdk-deprecated")
        ignoreFailures = false
    }

    // === SPOTBUGS ===
    spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
    spotbugs.toolVersion.set("4.9.3")

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        reports.create("html") {
            required.set(true)
            outputLocation = project.layout.buildDirectory.file("reports/spotbugs.html").get().asFile
            setStylesheet("fancy-hist.xsl")
        }
    }

    // === PUBLISHING ===
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.publishToMavenLocal)
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

}

allprojects {
    // versions plugin configuration
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|alpha|beta|b|M)(-?[0-9]*)?".toRegex()
        val isStable = stableKeyword || !regex.matches(version)
        return isStable
    }

    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            !isStable(candidate.version)
        }
    }
}
