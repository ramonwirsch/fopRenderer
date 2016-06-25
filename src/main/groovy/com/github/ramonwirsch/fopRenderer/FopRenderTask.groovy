package com.github.ramonwirsch.fopRenderer

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.apache.fop.cli.InputHandler
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

/**
 * Created by ramonw on 06.11.15.
 */
@ParallelizableTask
class FopRenderTask extends DefaultTask {

    public FopRenderTask() {
        group 'build'
		description 'Render all available PDFs using fop'
    }

    @InputFiles
    def FileCollection inputFiles

    @InputDirectory
    def File baseDir

    @OutputDirectory
    def File outputDir = new File(project.buildDir, 'doc')

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        boolean success = true;

        inputs.outOfDate { change ->
            if (change.file.absolutePath.endsWith('.fo')) {
                logger.info "out of date: ${change.file.name}"
                def outFile = getOutputFile(change.file)
                success &= run(change.file, outFile)
            } else {
                logger.debug('ingoring {}', change.file)
            }
        }

        inputs.removed {
            change ->
                logger.info "removed: ${change.file.name}"
                getOutputFile(change.file).delete()
        }

        if (!success)
            throw new TaskExecutionException(this, new Exception("One of the inputs failed to render. See log output"))
    }


    boolean run(File inputFile, outputFile) {
        logger.info('Rendering {}', inputFile)
        OutputStream outstream = null

        try {
            InputHandler input = new InputHandler(inputFile)
            FOUserAgent userAgent = FopFactory.newInstance().newFOUserAgent()
            userAgent.baseURL = baseDir.toURI().toURL().toExternalForm()

            outstream = new BufferedOutputStream(new FileOutputStream(outputFile))

            input.renderTo(userAgent, MimeConstants.MIME_PDF, outstream)
            logger.info('Successfully rendered {}', outputFile)
            return true
        } catch (Exception e) {
            logger.error('Unknown Exception in FOP', e)
//            outputFile.delete()
            throw new TaskExecutionException(this, e)
        } finally {
            try {
                if (outstream)
                    outstream.close()
            } catch (Exception e) {
                logger.error('Unknown Exception in FOP', e)
            }
        }
    }

    File getOutputFile(File inFile) {
        return new File(outputDir, inFile.name.replace('.fo', '.pdf'))
    }

}
