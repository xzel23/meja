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
