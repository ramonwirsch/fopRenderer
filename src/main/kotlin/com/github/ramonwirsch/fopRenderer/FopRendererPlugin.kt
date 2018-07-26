package com.github.ramonwirsch.fopRenderer

import com.github.ramonwirsch.fopRenderer.validation.XMLValidatorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.TaskProvider

open class FopRendererPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.pluginManager.apply(BasePlugin::class.java)

		val schemas = project.container(SchemaConfigExtension::class.java) { name -> SchemaConfigExtension(name, project) }

		val render = project.container(RenderConfigExtension::class.java) { name -> RenderConfigExtension(name, project) }

		val fopRenderer = project.extensions.create("fopRenderer", FopRenderExtension::class.java, schemas, render)

		val tasks = project.tasks

		val allValidationTasks = mutableListOf<TaskProvider<XMLValidatorTask>>()

		fopRenderer.schemas.all {
			val schemaConfig = this
			val currentValidationTask = tasks.register("validate" + schemaConfig.name.capitalize(), XMLValidatorTask::class.java) { this.schemaConfig = schemaConfig }

			allValidationTasks += currentValidationTask
		}

		val validateAllTask = tasks.register("validate") {
			group = "verification"
			description = "Validate all XML files"

			dependsOn(allValidationTasks)
		}

		tasks.named("check").configure {
			dependsOn(validateAllTask)
		}

		val allRenderTasks = mutableListOf<TaskProvider<FopRenderTask>>()

		fopRenderer.render.all {
			val renderConfig = this

			val currentTransformTask = tasks.register("transform" + renderConfig.name.capitalize(), XSLTTransformTask::class.java) {
				this.renderConfig = renderConfig

				val outputFileName = renderConfig.name

				outputFileProperty.set(project.layout.buildDirectory.file("fo/$outputFileName.fo"))

				if (renderConfig.isRequiresValidation) {
					dependsOn(validateAllTask)
				}
			}

			renderConfig.transformTask = currentTransformTask

			val currentRenderTask = tasks.register("render" + renderConfig.name.capitalize(), FopRenderTask::class.java) {
				this.renderConfig = renderConfig
				inputFileProperty.set(currentTransformTask.get().outputFileProperty)
			}

			renderConfig.renderTask = currentRenderTask

			allRenderTasks += currentRenderTask
		}

		tasks.named("build").configure {
			dependsOn(allRenderTasks)
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