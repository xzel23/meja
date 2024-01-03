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

import java.net.URI
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("idea")
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
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

    project.setVersion(rootProject.libs.versions.projectVersion.get())
    val isReleaseVersion = !project.getVersion().toString().endsWith("SNAPSHOT")

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "com.dua3.cabe")

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
        withJavadocJar()
        withSourcesJar()
    }

    // dependencies
    dependencies {
        // Cabe (source annotations)
        compileOnly(rootProject.libs.cabe.annotations)

        // LOG4J2
        implementation(rootProject.libs.log4j.api)
        testRuntimeOnly(rootProject.libs.log4j.core)

        // JUnit
        testImplementation(rootProject.libs.junit.jupiter.api)
        testImplementation(rootProject.libs.junit.jupiter.params)
        testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
    }

    idea {
        module {
            inheritOutputDirs = false
            outputDir = project.layout.buildDirectory.file("classes/java/main/").get().asFile
            testOutputDir = project.layout.buildDirectory.file("classes/java/test/").get().asFile
        }
    }

    tasks.test {
        useJUnitPlatform()
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
                url = if (isReleaseVersion) releaseRepo else snapshotRepo
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

    // === SPOTBUGS ===
    spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>() {
        reports.create("html") {
            required.set(true)
            outputLocation = project.layout.buildDirectory.file("reports/spotbugs.html").get().asFile
            setStylesheet("fancy-hist.xsl")
        }
    }

    // === PUBLISHING ===
    tasks.withType<PublishToMavenRepository>() {
        dependsOn(tasks.publishToMavenLocal)
    }

    tasks.withType<Jar>() {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

}

defaultTasks = mutableListOf("build", "publishToMavenLocal")
