package com.github.ramonwirsch.fopRenderer;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.cli.InputHandler;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ramonw on 06.11.15.
 */
@ParallelizableTask
public class FopRenderTask extends DefaultTask {

	private final File outputDir = new File(getProject().getBuildDir(), "doc");
	private final Logger logger = getLogger();
	private RenderConfigExtension renderConfig;
	private File input;
	private boolean update;

	public FopRenderTask() {
		setGroup("build");
		setDescription("Render all available PDFs using fop");
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

		boolean success = render(getInput(), getOutputFile());

        if (!success)
			throw new TaskExecutionException(this, new Exception("One of the inputs failed to render. See log output"));
	}


	private boolean render(File inputFile, File outputFile) {
		logger.info("Rendering {}", inputFile);
		OutputStream outstream = null;

        try {
			InputHandler input = new InputHandler(inputFile);
			FOUserAgent userAgent = FopFactory.newInstance().newFOUserAgent();
			userAgent.setBaseURL(renderConfig.getResourcesBaseDir().toURI().toURL().toExternalForm());

			outstream = new BufferedOutputStream(new FileOutputStream(outputFile));

			input.renderTo(userAgent, MimeConstants.MIME_PDF, outstream);
			logger.info("Successfully rendered {}", outputFile);
			return true;
		} catch (Exception e) {
			logger.error("Unknown Exception in FOP", e);
//            outputFile.delete()
			throw new TaskExecutionException(this, e);
		} finally {
            try {
				if (outstream != null)
					outstream.close();
			} catch (Exception e) {
				logger.error("Unknown Exception in FOP", e);
			}
        }
    }

	@InputFiles
	public FileCollection getResources() {
		File resourcesBaseDir = renderConfig.getResourcesBaseDir();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> resourceCollectionParams = renderConfig.getResourceCollectionParams();
		if (resourceCollectionParams != null)
			params.putAll(resourceCollectionParams);

		params.put("dir", resourcesBaseDir);
		return getProject().fileTree(params);
	}

	@OutputFile
	public File getOutputFile() {
		return new File(outputDir, getInput().getName().replace(".fo", ".pdf"));
	}

	@Internal
	private File getRootSrc() {
		return renderConfig.getRootSrc();
	}

	@InputFile
	public File getInput() {
		return input;
	}

	public void setInput(File input) {
		this.input = input;
	}

	@Internal
	public RenderConfigExtension getRenderConfig() {
		return renderConfig;
	}

	public void setRenderConfig(RenderConfigExtension renderConfig) {
		this.renderConfig = renderConfig;
	}
}
