package com.github.ramonwirsch.fopRenderer

import com.github.ramonwirsch.fopRenderer.validation.XMLValidatorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class FopRendererPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.apply {
			it.plugin("base")
		}

		val schemas = project.container(SchemaConfigExtension::class.java) { name -> SchemaConfigExtension(name, project) }

		val render = project.container(RenderConfigExtension::class.java)

		val fopRenderer = project.extensions.create("fopRenderer", FopRenderExtension::class.java, schemas, render)

		val tasks = project.tasks

		val validateAllTask = tasks.create("validate") { it ->
			it.group = "verification"
			it.description = "Validate all XML files"
		}

		tasks.findByName("check").dependsOn(validateAllTask)

		project.afterEvaluate { _ ->
			fopRenderer.schemas.forEach { schemaConfig ->
				val currentValidationTask = tasks.create("validate" + schemaConfig.name.capitalize(), XMLValidatorTask::class.java) { task -> task.schemaConfig = schemaConfig }

				validateAllTask.dependsOn(currentValidationTask)
			}

			val buildTask = tasks.findByName("build")

			fopRenderer.render.forEach { renderConfig ->

				val currentTransformTask = tasks.create("transform" + renderConfig.name.capitalize(), XSLTTransformTask::class.java) { task ->
					task.renderConfig = renderConfig
					task.dependsOn(validateAllTask)
				}

				val currentRenderTask = tasks.create("render" + renderConfig.name.capitalize(), FopRenderTask::class.java) { task ->
					task.renderConfig = renderConfig
					task.input = currentTransformTask.outputFile
					task.dependsOn(currentTransformTask)
				}

				buildTask.dependsOn(currentRenderTask)
			}
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