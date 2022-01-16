package com.github.ramonwirsch.fopRenderer

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.apache.fop.cli.InputHandler
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.*
import org.gradle.workers.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.MalformedURLException
import javax.inject.Inject

@CacheableTask
open class FopRenderTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Internal
    var renderConfig: RenderConfigExtension? = null

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputFile
    val inputFileProperty: RegularFileProperty

    @get:OutputFile
    val outputFileProperty: RegularFileProperty
    private var update = false

    init {
        group = "build"
        description = "Render all available PDFs using fop"
        inputFileProperty = project.objects.fileProperty()
        outputFileProperty = project.objects.fileProperty()
        val outputFileRel = project.provider { "doc/" + renderConfig!!.name + ".pdf" }
        outputFileProperty.set(project.layout.buildDirectory.file(outputFileRel))
    }

    @TaskAction
    private fun execute() {
        try {
            val resourceBaseDir = renderConfig!!.resourcesBaseDir
            workerExecutor.noIsolation().submit(RenderWorker::class.java) {
                inputFile.set(this@FopRenderTask.inputFile)
                outputFile.set(this@FopRenderTask.outputFile)
                this.resourceBaseDir.set(resourceBaseDir)
            }
        } catch (e: MalformedURLException) {
            logger.error("ResourceBaseDir is incorrectly configured!", e)
        }
    }

    interface RenderParameters: WorkParameters {
        val inputFile: RegularFileProperty
        val outputFile: RegularFileProperty
        val resourceBaseDir: RegularFileProperty
    }

    abstract class RenderWorker : WorkAction<RenderParameters> {

        private val logger = Logging.getLogger(this.javaClass)

        override fun execute() {
            val params = parameters
            val inputFile = params.inputFile.asFile.get()
            val outputFile = params.outputFile.asFile.get()
            val resourceBaseDir = params.resourceBaseDir.asFile.get()

            var outstream: OutputStream? = null
            try {
                val input = InputHandler(inputFile)
                val userAgent: FOUserAgent = FopFactory.newInstance(resourceBaseDir.toURI()).newFOUserAgent()
                outstream = BufferedOutputStream(FileOutputStream(outputFile))
                input.renderTo(userAgent, MimeConstants.MIME_PDF, outstream)
                logger.info("Successfully rendered {}", outputFile)
            } catch (e: Exception) {
                logger.error("Unknown Exception in FOP", e)
                throw RuntimeException(e)
            } finally {
                try {
                    outstream?.close()
                } catch (e: Exception) {
                    logger.error("Unknown Exception in FOP", e)
                }
            }
        }
    }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val resources: FileCollection
        get() {
            val resourcesBaseDir = renderConfig!!.resourcesBaseDir
            val resourceCollectionParams: Map<String, Any?> = renderConfig!!.resourceCollectionParams
            val params: MutableMap<String, Any?> = HashMap(resourceCollectionParams)
            params["dir"] = resourcesBaseDir
            return project.fileTree(params)
        }

    @get:Internal
    var outputFile: File?
        get() = outputFileProperty.asFile.get()
        set(file) {
            outputFileProperty.set(file)
        }

    private val rootSrc: File
        get() = renderConfig!!.rootSrc

    @get:Internal
    var inputFile: File?
        get() = inputFileProperty.asFile.get()
        set(input) {
            inputFileProperty.set(input)
        }
}