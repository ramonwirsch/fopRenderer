package com.github.ramonwirsch.fopRenderer

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.file.FileCollection
import java.io.File
import java.net.MalformedURLException
import java.net.URL

class SchemaConfigExtension(
		val name: String,
		val project: Project
) {
	companion object {
		/**
		 * Placeholder to configure plugin for implicit schemas (the ones that are configured within the xml files themselves)
		 */
		val FALLBACK_URL = URL("http://auto-implicit-validation")
	}

	/**
	 * Directory containing all the schemas
	 *
	 */
	var schemaDir: File? = null

	/**
	 * Local backup location for schemas
	 *
	 */
	var offlineSchema: File? = null

	/**
	 * force use of XML schemas configured inside source files
	 *
	 */
	var isUseInherentSchemas = false

	private var _files: FileCollection? = null

	/**
	 * Files to validate
	 *
	 */
	var files: FileCollection
		get() = _files ?: throw ProjectConfigurationException("No files have been configured for SchemaConfig $name", null)
		set(value) {
			_files = project.files(value)
		}

	/**
	 * Files to validate
	 *
	 * @param file single file to validate
	 */
	fun setFiles(file: File) {
		files = project.files(file)
	}

	/**
	 * Get the configured schema
	 * Defaults to offlineSchemas or implicit schemas
	 *
	 */
	var schemaUri: URL? = null
		get() = field ?: offlineSchema?.let { it.toURI().toURL() } ?: FALLBACK_URL

	/**
	 * Set the schema to use for validation
	 * Defaults to implicit schemas
	 *
	 * @param file local schema file
	 * @throws MalformedURLException in case of invalid URL
	 */
	@Throws(MalformedURLException::class)
	fun setSchemaUri(file: File) {
		schemaUri = file.toURI().toURL()
	}

	/**
	 * Set the schema to use for validation
	 * Defaults to implicit schemas
	 *
	 * @param uri schema URL
	 * @throws MalformedURLException in case of invalid URL
	 */
	@Throws(MalformedURLException::class)
	fun setSchemaUri(uri: String) {
		schemaUri = URL(uri)
	}

	/**
	 * List of additional files whose changes require a new validation
	 *
	 */
	var dependencies: FileCollection? = null
}