package com.github.ramonwirsch.gradleKotlin

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree

fun Project.fileTree(baseDir: Any, configureAction: ConfigurableFileTree.() -> Unit) = this.fileTree(baseDir, { it.configureAction() })