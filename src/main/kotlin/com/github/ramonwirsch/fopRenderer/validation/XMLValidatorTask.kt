package com.github.ramonwirsch.fopRenderer.validation

import com.github.ramonwirsch.fopRenderer.FopRendererPlugin
import com.github.ramonwirsch.fopRenderer.SchemaConfigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskInstantiationException
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import org.xml.sax.SAXException
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.xml.parsers.ParserConfigurationException

open class XMLValidatorTask
@Inject constructor(
		private val workerExecutor: WorkerExecutor
) : DefaultTask() {

	@get:Internal
	lateinit var schemaConfig: SchemaConfigExtension
	@get:Internal
	var isFullValidation = false

	init {
		group = "verification"
	}

	val inputFiles: FileCollection
		@InputFiles
		@PathSensitive(PathSensitivity.RELATIVE)
		get() = schemaConfig.files

	val schemaFiles: FileCollection
		@InputFiles
		@PathSensitive(PathSensitivity.RELATIVE)
		get() {
			val project = project
			val dir = if (schemaConfig.schemaDir != null) schemaConfig.schemaDir else project.projectDir
			return project.fileTree(dir) {
				include("*.xsd")
			}
		}

	val schemaUri: URL
		@Input
		get() {
			if (FopRendererPlugin.isOffline(project) || schemaConfig.schemaUri == null) {
				if (schemaConfig.offlineSchema == null)
					throw TaskInstantiationException(String.format("Offline schema validation requested but no offline schema given for '%s'", schemaConfig.name))
				else
					try {
						return schemaConfig.offlineSchema.toURI().toURL()
					} catch (e: MalformedURLException) {
						throw RuntimeException(e)
					}

			} else return if (!schemaConfig.isUseInherentSchemas)
				schemaConfig.schemaUri!!
			else
				SchemaConfigExtension.FALLBACK_URL
		}

	@Internal
	override fun getDescription() = "Validate all files of SchemaConfig group '${schemaConfig.name}'"

	@TaskAction
	@Throws(ParserConfigurationException::class, SAXException::class)
	internal fun execute(inputs: IncrementalTaskInputs) {
		val validationSchema = if (!FopRendererPlugin.isOffline(project) && schemaConfig.isUseInherentSchemas) {
			logger.info("Using inherent schemas")
			null
		} else {
			val schemaUri = schemaUri
			logger.info("Using schema {}", schemaUri)
			schemaUri
		}

		val toValidate = HashSet<File>()

		if (!isFullValidation) {
			inputs.outOfDate {
				when {
					isFullValidation || file.isDirectory -> {
					}
					file.name.endsWith(".xsd") -> {
						isFullValidation = true
						logger.info("Schema {} changed, full validation!", file)
					}
					else -> {
						logger.info("XML out of date: {}", file)
						toValidate += file
					}
				}
			}

			inputs.removed {
				val name = file.name

				if (name.endsWith(".xsd")) {
					isFullValidation = true
					logger.info("Schema {} removed, full validation!", file)
				} else {
					logger.info("removed: {}", name)
				}
			}
		}

		if (isFullValidation) {
			toValidate.addAll(schemaConfig.files.files)
			logger.warn("Validating all inputs")
		}

		toValidate.forEach { v ->
			workerExecutor.submit(ValidationWorker::class.java) {
				isolationMode = IsolationMode.NONE
				setParams(validationSchema, v)
			}
		}
	}
}

private class ValidationWorker
@Inject constructor(
		validationSchema: URL?,
		val input: File
) : Runnable {

	private val validator = XMLValidatorFactory.forSchema(validationSchema).createValidator()

	override fun run() {
		validator.validateOrThrow(input)
	}

}