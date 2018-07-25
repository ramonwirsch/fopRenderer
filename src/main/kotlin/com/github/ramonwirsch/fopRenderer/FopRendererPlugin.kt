package com.github.ramonwirsch.fopRenderer

import com.github.ramonwirsch.fopRenderer.validation.XMLValidatorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

open class FopRendererPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.pluginManager.apply(BasePlugin::class.java)

		val schemas = project.container(SchemaConfigExtension::class.java) { name -> SchemaConfigExtension(name, project) }

		val render = project.container(RenderConfigExtension::class.java) { name -> RenderConfigExtension(name, project) }

		val fopRenderer = project.extensions.create("fopRenderer", FopRenderExtension::class.java, schemas, render)

		val tasks = project.tasks

		val validateAllTask = tasks.create("validate") {
			group = "verification"
			description = "Validate all XML files"
		}

		tasks.findByName("check")!!.dependsOn(validateAllTask)

		fopRenderer.schemas.all {
			val schemaConfig = this
			val currentValidationTask = tasks.create("validate" + schemaConfig.name.capitalize(), XMLValidatorTask::class.java) { this.schemaConfig = schemaConfig }

			validateAllTask.dependsOn(currentValidationTask)
		}

		val buildTask = tasks.findByName("build")!!

		fopRenderer.render.all {
			val renderConfig = this

			val currentTransformTask = tasks.create("transform" + renderConfig.name.capitalize(), XSLTTransformTask::class.java) {
				this.renderConfig = renderConfig

				val outputFileName = renderConfig.rootSrcProperty.asFile.map { "fo/${it.nameWithoutExtension}.fo" }

				outputFileProperty.set(project.layout.buildDirectory.file(outputFileName))

				if (renderConfig.isRequiresValidation) {
					dependsOn(validateAllTask)
				}
			}

			val currentRenderTask = tasks.create("render" + renderConfig.name.capitalize(), FopRenderTask::class.java) {
				this.renderConfig = renderConfig
				inputFileProperty.set(currentTransformTask.outputFileProperty)

				dependsOn(currentTransformTask)
			}

			buildTask.dependsOn(currentRenderTask)
		}
	}

	companion object {

		fun isOffline(project: Project): Boolean {
			if (project.gradle.startParameter.isOffline) {
				return true
			} else if (project.properties.containsKey("offlineSchemas")) {
				val offlineSchemas = project.properties["offlineSchemas"] ?: return false

				return java.lang.Boolean.valueOf(offlineSchemas.toString())!!
			} else {
				return false
			}
		}
	}
}