package com.github.ramonwirsch.fopRenderer

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.workers.*
import java.io.File
import javax.inject.Inject
import javax.xml.transform.TransformerException

@CacheableTask
open class XSLTTransformTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {
    @get:Internal
    var renderConfig: RenderConfigExtension? = null

    @get:OutputFile
    val outputFileProperty: RegularFileProperty

    init {
        group = "build"
        outputFileProperty = project.objects.fileProperty()
    }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val dependencies: FileCollection
        get() = renderConfig!!.dependencies

    @get:Internal
    var outputFile: File?
        get() = outputFileProperty.asFile.get()
        set(outputFile) {
            outputFileProperty.set(outputFile)
        }

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    val stylesheet: File
        get() = renderConfig!!.stylesheet

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val rootSrc: File
        get() = renderConfig!!.rootSrc

    @Internal
    override fun getDescription(): String {
        return String.format("Transform %s using the XSLT %s", renderConfig!!.rootSrc.name, stylesheet.name)
    }

    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(TransformWorker::class.java) {
            stylesheet.set(this@XSLTTransformTask.stylesheet)
            outputFile.set(this@XSLTTransformTask.outputFileProperty)
            rootSrc.set(renderConfig!!.rootSrc)
        }
    }

    interface TransformParameters: WorkParameters {
        val stylesheet: RegularFileProperty
        val outputFile: RegularFileProperty
        val rootSrc: RegularFileProperty
    }

    abstract class TransformWorker : WorkAction<TransformParameters> {
        override fun execute() {
            val params = parameters

            val transformer = XSLTTransformation(params.stylesheet.asFile.get(), emptyMap())
            try {
                transformer.transform(params.rootSrc.asFile.get(), params.outputFile.asFile.get())
            } catch (e: TransformerException) {
                throw RuntimeException(e)
            }
        }
    }
}
