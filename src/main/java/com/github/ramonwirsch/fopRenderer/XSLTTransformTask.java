package com.github.ramonwirsch.fopRenderer;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
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

import java.io.File;
import java.util.Collections;
import javax.inject.Inject;

/**
 * Created by ramonw on 06.11.15.
 */
@CacheableTask
public class XSLTTransformTask extends DefaultTask {

	private final WorkerExecutor workerExecutor;
	private RenderConfigExtension renderConfig;

	@Inject
	public XSLTTransformTask(WorkerExecutor workerExecutor) {
		this.workerExecutor = workerExecutor;
		setGroup("build");
	}

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public FileCollection getSrcDependencies() {
		return (renderConfig.getDependencies() != null) ? renderConfig.getDependencies() : getProject().fileTree(renderConfig.getRootSrc().getParentFile());
	}

    @OutputFile
	public File getOutputFile() {
		return new File(new File(getProject().getBuildDir(), "fo"), getRenderConfig().getRootSrc().getName().replace(".xml", ".fo"));
	}

	@InputFile
	@PathSensitive(PathSensitivity.NONE)
	public File getStylesheet() {
		return renderConfig.getStylesheet();
	}

	@InputFile
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

			transformer.transform(rootSrc, outputFile);
		}
	}
}
