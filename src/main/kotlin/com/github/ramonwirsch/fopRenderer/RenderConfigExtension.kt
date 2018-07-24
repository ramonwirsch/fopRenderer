package com.github.ramonwirsch.fopRenderer

import org.gradle.api.file.FileCollection
import java.io.File

class RenderConfigExtension(val name: String) {

	/**
	 * Root source file to render
	 */
	lateinit var rootSrc: File

	/**
	 * Stylesheet to use
	 */
	lateinit var stylesheet: File

	/**
	 * List of files xml files that are included by [rootSrc] and whose changes require a new rendering
	 *
	 * Defaults to all files in parent dir of [rootSrc]
	 */
	var dependencies: FileCollection? = null

	/**
	 * directory, relative to which to find resources (graphics etc...)
	 *
	 * Defaults to parent of [.getRootSrc]
	 */
	var resourcesBaseDir: File? = null
		get() = if (field != null) field else rootSrc.parentFile

	/**
	 * Additional params fed to gradles [org.gradle.api.Project.fileTree] method
	 */
	var resourceCollectionParams: Map<String, Any> = mapOf("exclude" to "**/*.xml")

	/**
	 * whether all validations must have passed before trying to render
	 */
	var isRequiresValidation: Boolean = true
}