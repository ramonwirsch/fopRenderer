package com.github.ramonwirsch.fopRenderer

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.TaskInstantiationException
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

/**
 * Created by ramonw on 06.11.15.
 */
@ParallelizableTask
class XMLValidatorTask extends DefaultTask {

	private static final FALLBACK_URL = new URL("http://auto-implicit-validation");

    public XMLValidatorTask() {
		group 'verification'
	}

	SchemaConfigExtension schemaConfig

    @InputFiles
    FileCollection getInputFiles() {
		schemaConfig.files
	}

	@InputFiles
	FileCollection getSchemaFile() {
		File dir = schemaConfig.schemaDir ?: project.projectDir
		project.fileTree(dir:dir, include: '*.xsd');
	}

    @Input
	URL getSchemaUri() {
		if (FopRendererPlugin.isOffline(project) || !schemaConfig.schemaUri) {
			if (schemaConfig.offlineSchema == null)
				throw new TaskInstantiationException("Offline schema validation requested but no offline schema given for '${schemaConfig.name}'")
			else
				return schemaConfig.offlineSchema.toURI().toURL()
		} else if (!schemaConfig.useInherentSchemas)
			return schemaConfig.schemaUri;
		else
			return FALLBACK_URL;
	}

	@Override
	String getDescription() {
		"Validate all files of SchemaConfig group '${schemaConfig.name}'"
	}

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
		XMLValidator validator
		if (!FopRendererPlugin.isOffline(project) && schemaConfig.useInherentSchemas) {
			logger.info("Using inherent schemas")
			validator = new XMLValidator(null)
		} else {
			logger.info("Using schema {}", schemaUri)
			validator = new XMLValidator(schemaUri)
		}

        boolean success = true;

		boolean fullValidation = false;
		Set<File> toValidate = []

        inputs.outOfDate { change ->
			if (fullValidation)
				return
			if (change.file.name.endsWith(".xsd")) {
				fullValidation = true
				return
			}

            logger.info "out of date: ${change.file.name}"
			toValidate += change.file
        }

        inputs.removed { change ->
			if (change.file.name.endsWith(".xsd")) {
				fullValidation = true
				return;
			}

            logger.info "removed: ${change.file.name}"
        }

		if (fullValidation) {
			toValidate = inputFiles.files
			logger.warn "schema change detected, full validation required!"
		}

		toValidate.each {
			success &= validator.validate(it);
		}

        if (!success)
            throw new TaskExecutionException(this, new Exception("One of the inputs failed to validate. See log output"))
    }
}
