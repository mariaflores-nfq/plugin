
// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdea
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity
import org.jetbrains.intellij.platform.gradle.models.ProductRelease.Channel.RELEASE

plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = "com.bbva.gkxj.atiframework"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.1.5.1")
        bundledPlugin("com.intellij.java")
    }
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("com.toedter:jcalendar:1.4")
    implementation("com.github.java-json-tools:json-schema-validator:2.2.14")
    implementation("com.github.java-json-tools:json-schema-core:1.2.14")
    implementation("com.github.java-json-tools:jackson-coreutils:2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("org.swinglabs:swingx:1.6.1")
    implementation("org.mongodb:mongodb-driver-sync:5.1.1")
    implementation(files("libs/jgraphx-4.2.2.jar"))
    implementation("com.fifesoft:rsyntaxtextarea:3.5.3")
    implementation("com.fifesoft:autocomplete:3.3.1")
}

intellijPlatform {
  buildSearchableOptions = false

  pluginConfiguration {
    ideaVersion {
      sinceBuild = "251"
    }
  }
  pluginVerification  {
    ides {
      // since 253, IntelliJ IDEA Community and Ultimate have been merged into IntelliJ IDEA
      select {
        types = listOf(IntellijIdeaCommunity)
        untilBuild = "252.*"
      }
      select {
        types = listOf(IntellijIdea)
        sinceBuild = "253"
        channels = listOf(RELEASE)
      }
    }
  }
}

