package com.github.ramonwirsch.fopRenderer;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.cli.InputHandler;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ramonw on 06.11.15.
 */
@CacheableTask
public class FopRenderTask extends DefaultTask {

	private final Logger logger = getLogger();
	private final WorkerExecutor workerExecutor;
	private RenderConfigExtension renderConfig;
	private final RegularFileProperty inputFileProperty;
	private final RegularFileProperty outputFileProperty;
	private boolean update;

	@Inject
	public FopRenderTask(WorkerExecutor workerExecutor) {
		setGroup("build");
		setDescription("Render all available PDFs using fop");
		this.workerExecutor = workerExecutor;
		inputFileProperty = getProject().getObjects().fileProperty();
		outputFileProperty = getProject().getObjects().fileProperty();

		Provider<String> outputFileRel = getProject().provider(() -> "doc/"+renderConfig.getName()+".pdf");

		outputFileProperty.set(getProject().getLayout().getBuildDirectory().file(outputFileRel));
	}

    @TaskAction
	private void execute(IncrementalTaskInputs inputs) {
		update = false;

		inputs.outOfDate(change -> {
			File file = change.getFile();
			logger.info("out of date: {}", file.getName());
			update = true;
		});

		inputs.removed(change -> {
			File file = change.getFile();
			logger.info("removed: {}", file);
			update = true;
		});



		try {
			String resourceBaseDir = renderConfig.getResourcesBaseDir().toURI().toURL().toExternalForm();

			workerExecutor.submit(RenderWorker.class, (config) -> {
				config.setIsolationMode(IsolationMode.NONE);
				config.setParams(getInputFile(), getOutputFile(), resourceBaseDir);
			});
		} catch (MalformedURLException e) {
			logger.error("ResourceBaseDir is incorrectly configured!", e);
		}


	}

	private static class RenderWorker implements Runnable {
		private Logger logger = Logging.getLogger(this.getClass());

		private File inputFile;
		private File outputFile;
		private String resourceBaseDir;

		@Inject
		public RenderWorker(File inputFile, File outputFile, String resourceBaseDir) {
			this.inputFile = inputFile;
			this.outputFile = outputFile;
			this.resourceBaseDir = resourceBaseDir;
		}

		@Override
		public void run() {
			OutputStream outstream = null;

			try {
				InputHandler input = new InputHandler(inputFile);
				FOUserAgent userAgent = FopFactory.newInstance().newFOUserAgent();
				userAgent.setBaseURL(resourceBaseDir);

				outstream = new BufferedOutputStream(new FileOutputStream(outputFile));

				input.renderTo(userAgent, MimeConstants.MIME_PDF, outstream);
				logger.info("Successfully rendered {}", outputFile);
			} catch (Exception e) {
				logger.error("Unknown Exception in FOP", e);
				throw new RuntimeException(e);
			} finally {
				try {
					if (outstream != null)
						outstream.close();
				} catch (Exception e) {
					logger.error("Unknown Exception in FOP", e);
				}
			}
		}
	}

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public FileCollection getResources() {
		File resourcesBaseDir = renderConfig.getResourcesBaseDir();
		Map<String, Object> resourceCollectionParams = renderConfig.getResourceCollectionParams();
		Map<String, Object> params = new HashMap<>(resourceCollectionParams);

		params.put("dir", resourcesBaseDir);
		return getProject().fileTree(params);
	}



	@OutputFile
	@PathSensitive(PathSensitivity.NAME_ONLY)
	public RegularFileProperty getOutputFileProperty() {
		return outputFileProperty;
	}

	@Internal
	public File getOutputFile() {
		return outputFileProperty.getAsFile().get();
	}

	public void setOutputFile(File file) {
		outputFileProperty.set(file);
	}

	private File getRootSrc() {
		return renderConfig.getRootSrc();
	}

	@InputFile
	@PathSensitive(PathSensitivity.NAME_ONLY)
	public RegularFileProperty getInputFileProperty() {
		return inputFileProperty;
	}

	@Internal
	public File getInputFile() {
		return inputFileProperty.getAsFile().get();
	}

	public void setInputFile(File input) {
		inputFileProperty.set(input);
	}

	@Internal
	public RenderConfigExtension getRenderConfig() {
		return renderConfig;
	}

	public void setRenderConfig(RenderConfigExtension renderConfig) {
		this.renderConfig = renderConfig;
	}
}
