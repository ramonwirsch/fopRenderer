package com.github.ramonwirsch.fopRenderer;

import com.github.ramonwirsch.fopRenderer.util.CollectionUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.TaskInstantiationException;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ramonw on 06.11.15.
 */
@ParallelizableTask
public class XMLValidatorTask extends DefaultTask {

	private final Logger logger = getLogger();
	private SchemaConfigExtension schemaConfig;
	private boolean fullValidation = false;

    public XMLValidatorTask() {
		setGroup("verification");
	}

	@InputFiles
	public FileCollection getInputFiles() {
		return schemaConfig.getFiles();
	}

	@Internal
	public SchemaConfigExtension getSchemaConfig() {
		return schemaConfig;
	}

	public void setSchemaConfig(SchemaConfigExtension schemaConfig) {
		this.schemaConfig = schemaConfig;
	}

	@InputFiles
	public FileCollection getSchemaFiles() {
		Project project = getProject();
		File dir = schemaConfig.getSchemaDir() != null ? schemaConfig.getSchemaDir() : project.getProjectDir();
		return project.fileTree(CollectionUtil.immutableMap("dir", dir, "include", "*.xsd"));
	}

    @Input
	public URL getSchemaUri() {
		if (FopRendererPlugin.isOffline(getProject()) || schemaConfig.getSchemaUri() != null) {
			if (schemaConfig.getOfflineSchema() == null)
				throw new TaskInstantiationException(String.format("Offline schema validation requested but no offline schema given for '%s'", schemaConfig.getName()));
			else
				try {
					return schemaConfig.getOfflineSchema().toURI().toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
		} else if (!schemaConfig.isUseInherentSchemas())
			return schemaConfig.getSchemaUri();
		else
			return SchemaConfigExtension.FALLBACK_URL;
	}

	@Override
	@Internal
	public String getDescription() {
		return String.format("Validate all files of SchemaConfig group '%s'", schemaConfig.getName());
	}

	@Internal
	public boolean isFullValidation() {
		return fullValidation;
	}

	public void setFullValidation(boolean fullValidation) {
		this.fullValidation = fullValidation;
	}

	@TaskAction
	void execute(IncrementalTaskInputs inputs) throws ParserConfigurationException, SAXException {
		XMLValidator validator;
		if (!FopRendererPlugin.isOffline(getProject()) && schemaConfig.isUseInherentSchemas()) {
			logger.info("Using inherent schemas");
			validator = new XMLValidator(null);
		} else {
			URL schemaUri = getSchemaUri();
			logger.info("Using schema {}", schemaUri);
			validator = new XMLValidator(schemaUri);
		}

        boolean success = true;

		final Set<File> toValidate = new HashSet<>();

		if (!fullValidation) {
			inputs.outOfDate(change -> {
				if (fullValidation)
					return;

				File file = change.getFile();
				String name = file.getName();

				if (name.endsWith(".xsd")) {
					fullValidation = true;
					return;
				}

				logger.info("out of date: {}", name);
				toValidate.add(file);
			});

			inputs.removed(change -> {
				String name = change.getFile().getName();
				if (name.endsWith(".xsd")) {
					fullValidation = true;
					return;
				}

				logger.info("removed: {}", name);
			});
		}

		if (fullValidation) {
			toValidate.addAll(getSchemaConfig().getFiles().getFiles());
			logger.warn("schema change detected, full validation required!");
		}

		success = toValidate.stream().allMatch(validator::validate);

        if (!success)
			throw new TaskExecutionException(this, new Exception("One of the inputs failed to validate. See log output"));
	}
}
