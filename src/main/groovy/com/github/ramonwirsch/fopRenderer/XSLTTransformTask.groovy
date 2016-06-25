package com.github.ramonwirsch.fopRenderer

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Created by ramonw on 06.11.15.
 */
class XSLTTransformTask extends DefaultTask {

	public XSLTTransformTask() {
		group 'build'
	}

	RenderConfigExtension renderConfig

    @InputFile
    File getRootFile() {
		renderConfig.src
	}

	@InputFiles
	FileCollection getSrcDependencies() {
		(renderConfig.dependencies)?: project.fileTree(renderConfig.src.parentFile)
	}

    @OutputFile
    File getOutputFile() {
		new File(new File(project.buildDir, 'fo'), rootFile.name.replace('.xml', '.fo'))
	}

    @InputFile
    File getStylesheet() {
		renderConfig.stylesheet
	}

	@Override
	String getDescription() {
		return "Transform ${rootFile.name} using the XSLT ${stylesheet.name}"
	}

	@TaskAction
    void run() {
        XSLTTransformation transformer = new XSLTTransformation(stylesheet, Collections.emptyMap());

        transformer.transform(rootFile, outputFile);
    }

}
