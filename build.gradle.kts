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

import java.net.URI;
import com.adarshr.gradle.testlogger.theme.ThemeType;

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("idea")
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.github.spotbugs") version "5.0.9"
    id("com.dua3.cabe") version "1.0.0"
}

/////////////////////////////////////////////////////////////////////////////
object meta {
    val group           = "com.dua3.meja"
    val version         = "3.1.0"
    val scm             = "https://gitlab.com/com.dua3/lib/meja.git"
    val repo            = "public"
    val licenseName     = "The Apache Software License, Version 2.0"
    val licenseUrl      = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    val developerId     = "axh"
    val developerName   = "Axel Howind"
    val developerEmail  = "axh@dua3.com"
    val organization    = "dua3"
    val organizationUrl = "https://www.dua3.com"
}
/////////////////////////////////////////////////////////////////////////////

val isReleaseVersion = !meta.version.endsWith("SNAPSHOT")

val javafxVersion       by extra { "18.0.2" }
val dua3UtilityVersion  by extra { "10.1.0" }

subprojects {

    project.setVersion(meta.version)

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "com.github.spotbugs")

    if (!name.endsWith("-fx") && !name.endsWith("-swing")) {
        apply(plugin = "com.dua3.cabe")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withJavadocJar()
        withSourcesJar()
    }

    // dependencies
    dependencies {
        // Cabe (source annotations)
        compileOnly(group = "com.dua3.cabe", name = "cabe-annotations", version = "1.0.0")

        // SLF4J
        implementation("org.slf4j:slf4j-api:2.0.0")
        
        // JUnit
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    }

    idea {
        module {
            inheritOutputDirs = false
            outputDir = file("$buildDir/classes/java/main/")
            testOutputDir = file("$buildDir/classes/java/test/")
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
                groupId    = meta.group
                artifactId = project.name
                version    = meta.version

                from(components["java"])

                pom {
                    withXml {
                        val root = asNode()
                        root.appendNode("description", project.description)
                        root.appendNode("name", project.name)
                        root.appendNode("url", meta.scm)
                    }

                    licenses {
                        license {
                            name.set(meta.licenseName)
                            url.set(meta.licenseUrl)
                        }
                    }
                    developers {
                        developer {
                            id.set(meta.developerId)
                            name.set(meta.developerName)
                            email.set(meta.developerEmail)
                            organization.set(meta.organization)
                            organizationUrl.set(meta.organizationUrl)
                        }
                    }

                    scm {
                        url.set(meta.scm)
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
        setRequired(isReleaseVersion && gradle.taskGraph.hasTask("publish"))
        sign(publishing.publications["maven"])
    }

    // === SPOTBUGS ===
    spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>() {
        reports.create("html") {
            required.set(true)
            outputLocation.set(file("$buildDir/reports/spotbugs.html"))
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
