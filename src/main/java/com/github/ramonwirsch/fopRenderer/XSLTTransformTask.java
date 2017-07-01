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

import java.io.File;
import java.util.Collections;

/**
 * Created by ramonw on 06.11.15.
 */
@CacheableTask
public class XSLTTransformTask extends DefaultTask {

	private RenderConfigExtension renderConfig;

	public XSLTTransformTask() {
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
		XSLTTransformation transformer = new XSLTTransformation(getStylesheet(), Collections.emptyMap());

		transformer.transform(getRenderConfig().getRootSrc(), getOutputFile());
	}
}
