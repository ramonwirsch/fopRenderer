package com.github.ramonwirsch.fopRenderer.validation

import com.github.ramonwirsch.fopRenderer.FopRendererPlugin
import com.github.ramonwirsch.fopRenderer.SchemaConfigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
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
		outputs.upToDateWhen { true }
	}

	val inputFiles: FileCollection
		@Incremental
		@InputFiles
		@PathSensitive(PathSensitivity.RELATIVE)
		get() = schemaConfig.files

	val schemaFiles: FileCollection
		@InputFiles
		@PathSensitive(PathSensitivity.RELATIVE)
		get() {
			val project = project
			val dir = schemaConfig.schemaDir ?: project.projectDir
			return project.fileTree(dir) {
				include("*.xsd")
			}
		}

	val schemaUri: URL
		@Input
		get() {
			if (FopRendererPlugin.isOffline(project) || schemaConfig.schemaUri == null) {
				val offlineSchema = schemaConfig.offlineSchema
				if (offlineSchema == null)
					throw TaskInstantiationException(String.format("Offline schema validation requested but no offline schema given for '%s'", schemaConfig.name))
				else
					try {
						return offlineSchema.toURI().toURL()
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
	internal fun execute(inputs: InputChanges) {
		val validationSchema = if (!FopRendererPlugin.isOffline(project) && schemaConfig.isUseInherentSchemas) {
			logger.info("Using inherent schemas")
			SchemaConfigExtension.FALLBACK_URL
		} else {
			val schemaUri = schemaUri
			logger.info("Using schema {}", schemaUri)
			schemaUri
		}

		val toValidate = HashSet<File>()

		if (!isFullValidation && inputs.isIncremental) {
			inputs.getFileChanges(inputFiles).forEach { change ->
				when {
					change.fileType == FileType.DIRECTORY -> {
						// Nothing to do, still up-to-date
					}
					change.changeType == ChangeType.REMOVED -> {
						logger.info("removed: {}", change.file)
						// Nothing To do, still up-to-date
					}
					else -> {
						val file = change.file
						logger.info("XML out of date: {}", file)
						toValidate += file
					}
				}
			}
		}

		if (isFullValidation || !inputs.isIncremental) {
			toValidate.addAll(schemaConfig.files.files)
			logger.warn("Validating all inputs")
		}

		toValidate.forEach { v ->
			workerExecutor.noIsolation().submit(ValidationWorker::class.java) {
				this.validationSchema.set(validationSchema)
				input.set(v)
			}
		}
	}

	interface ValidationParameters: WorkParameters {
		val validationSchema: Property<URL>
		val input: RegularFileProperty
	}

	abstract class ValidationWorker: WorkAction<ValidationParameters> {

		override fun execute() {
			val params = parameters
			val validationSchema = params.validationSchema.get()

			val validator = XMLValidatorFactory.forSchema(validationSchema).createValidator()

			validator.validateOrThrow(params.input.asFile.get())
		}
	}
}