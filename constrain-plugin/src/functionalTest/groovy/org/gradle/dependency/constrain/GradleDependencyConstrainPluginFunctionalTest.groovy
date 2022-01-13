/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.gradle.dependency.constrain

import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language

import java.nio.file.Files

/**
 * A simple functional test for the 'org.gradle.dependency.constrain.greeting' plugin.
 */
class GradleDependencyConstrainPluginFunctionalTest extends BaseFunctionalTest {

    @Language("json")
    private static final String JUNIT_CONSTRAINTS = """
{
  "version": "1.0.0",
  "dependencyConstraints": [
    {
      "group": "junit",
      "name": "junit",
      "suggestedVersion": "4.13.1",
      "rejectedVersions": [
        "[4.7,4.13]"
      ],
      "because": {
        "advisoryIdentifiers": [
          "CVE-2020-15250"
        ],
        "reason": "TemporaryFolder on unix-like systems does not limit access to created files"
      }
    }
  ]
}
""".trim()

    void applyConstraintsFile() {
        File gradleDirectory = new File(projectDir, "gradle")
        Files.createDirectories(gradleDirectory.toPath())
        writeString(new File(gradleDirectory, "dependency-constraints.json"), JUNIT_CONSTRAINTS)
    }

    void "can constrain project dependencies"() {
        given:
        // Setup the test build
        applyConstraintsFile()
        applyConstraintPlugin()
        writeString(new File(projectDir, "build.gradle"), """
        plugins {
            id 'java-library'
        }

        repositories {
            // Use Maven Central for resolving dependencies.
            mavenCentral()
        }

        dependencies {
            testImplementation("junit:junit:4.12")
        }

        task resolve {
            inputs.files(configurations.testRuntimeClasspath)
            doLast {
                configurations.testRuntimeClasspath.files.name.each {
                    println(it)
                }
            }
        }
""".stripMargin())

        when:
        // Run the build
        BuildResult result = succeed("resolve")

        then:
        result.getOutput().contains("junit-4.13.1.jar")
    }

    void "can constrain buildscript dependencies"() {
        given:
        // Setup the test build
        applyConstraintsFile()
        applyConstraintPlugin()
        writeString(new File(projectDir, "build.gradle"), """
        buildscript {
            dependencies {
                classpath("junit:junit:4.12")
            }
        }
        plugins {
            id 'java-library'
        }

        repositories {
            // Use Maven Central for resolving dependencies.
            mavenCentral()
        }

        task resolve {
            inputs.files(buildscript.configurations.classpath)
            doLast {
                buildscript.configurations.classpath.files.name.each {
                    println(it)
                }
            }
        }
""".stripMargin())

        when:
        // Run the build
        BuildResult result = succeed("resolve")

        then:
        result.getOutput().contains("junit-4.13.1.jar")
    }

}
