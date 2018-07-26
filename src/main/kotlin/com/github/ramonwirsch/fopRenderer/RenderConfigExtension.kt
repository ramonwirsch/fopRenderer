package com.github.ramonwirsch.fopRenderer

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setValue
import java.io.File

class RenderConfigExtension(val name: String, project: Project) {

	val rootSrcProperty: RegularFileProperty = project.layout.fileProperty()

	/**
	 * Root source file to render
	 */
	var rootSrc: File
		get() = rootSrcProperty.asFile.get()
		set(value) = rootSrcProperty.set(value)

	/**
	 * Stylesheet to use
	 */
	lateinit var stylesheet: File

	val resourcesBaseDirProperty: Property<File> = project.objects.property<File>().apply { set(rootSrcProperty.asFile.map { it.parentFile }) }

	/**
	 * List of files xml files that are included by [rootSrc] and whose changes require a new rendering
	 *
	 * Defaults to all files in parent dir of [rootSrc]
	 */
	var dependencies: FileCollection = project.fileTree(resourcesBaseDirProperty)

	/**
	 * directory, relative to which to find resources (graphics etc...)
	 *
	 * Defaults to parent of [.getRootSrc]
	 */
	var resourcesBaseDir: File by resourcesBaseDirProperty

	/**
	 * Additional params fed to gradles [org.gradle.api.Project.fileTree] method
	 */
	var resourceCollectionParams: Map<String, Any> = mapOf("exclude" to "**/*.xml")

	/**
	 * whether all validations must have passed before trying to render
	 */
	var isRequiresValidation: Boolean = true

	lateinit var transformTask: TaskProvider<XSLTTransformTask>

	lateinit var renderTask: TaskProvider<FopRenderTask>
}