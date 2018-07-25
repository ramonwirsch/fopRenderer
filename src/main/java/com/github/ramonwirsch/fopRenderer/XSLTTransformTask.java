package com.github.ramonwirsch.fopRenderer;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.Collections;

/**
 * Created by ramonw on 06.11.15.
 */
@CacheableTask
public class XSLTTransformTask extends DefaultTask {

	private final WorkerExecutor workerExecutor;
	private RenderConfigExtension renderConfig;
	private final RegularFileProperty outputProperty;

	@Inject
	public XSLTTransformTask(WorkerExecutor workerExecutor) {
		this.workerExecutor = workerExecutor;
		setGroup("build");
		outputProperty = newOutputFile();
	}

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public FileCollection getDependencies() {
		return renderConfig.getDependencies();
	}

    @Internal
	public File getOutputFile() {
		return outputProperty.getAsFile().get();
	}

	public void setOutputFile(File outputFile) {
		outputProperty.set(outputFile);
	}

	@OutputFile
	public RegularFileProperty getOutputFileProperty() {
		return outputProperty;
	}

	@InputFile
	@PathSensitive(PathSensitivity.NONE)
	public File getStylesheet() {
		return renderConfig.getStylesheet();
	}

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public File getRootSrc() {
		return renderConfig.getRootSrc();
	}

	@Override
	@Internal
	public String getDescription() {
		return String.format("Transform %s using the XSLT %s", getRenderConfig().getRootSrc().getName(), getStylesheet().getName());
	}

	@Internal
	public RenderConfigExtension getRenderConfig() {
		return renderConfig;
	}

	public void setRenderConfig(RenderConfigExtension renderConfig) {
		this.renderConfig = renderConfig;

	}

	@TaskAction
    void run() {
		workerExecutor.submit(TransformWorker.class, (config) -> {
			config.setIsolationMode(IsolationMode.NONE);
			config.setParams(getStylesheet(), getOutputFile(), renderConfig.getRootSrc());
		});
	}

	private static class TransformWorker implements Runnable {

		private final File stylesheet;
		private final File outputFile;
		private final File rootSrc;

		@Inject
		public TransformWorker(File stylesheet, File outputFile, File rootSrc) {

			this.stylesheet = stylesheet;
			this.outputFile = outputFile;
			this.rootSrc = rootSrc;
		}

		@Override
		public void run() {
			XSLTTransformation transformer = new XSLTTransformation(stylesheet, Collections.emptyMap());

			try {
				transformer.transform(rootSrc, outputFile);
			} catch (TransformerException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
