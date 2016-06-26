package com.github.ramonwirsch.fopRenderer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by ramon on 13.11.2015.
 */
class FopRendererPlugin implements Plugin<Project> {

	static boolean isOffline(Project project) {
		if (project.gradle.startParameter.offline) {
			return true;
		} else if (project.properties.containsKey('offlineSchemas'))
			return Boolean.valueOf(project.properties['offlineSchemas'])
		else return false
	}

	@Override
	void apply(Project project) {
		project.apply(plugin: 'base')

		def schemas = project.container(SchemaConfigExtension) {
			name -> new SchemaConfigExtension(name, project)
		}
		def render = project.container(RenderConfigExtension)

		project.extensions.create('fopRenderer', FopRenderExtension, schemas, render)

		Task validateAllTask = project.tasks.create('validate') {
			group 'verification'
			description 'Validate all XML files'
		}

		project.tasks.findByName('check').dependsOn validateAllTask

		project.afterEvaluate {
			project.fopRenderer.schemas.each { schemaConfig ->
				Task currentValidationTask = project.tasks.create("validate${schemaConfig.name.capitalize()}", XMLValidatorTask) { task ->
					task.schemaConfig = schemaConfig
				}
				validateAllTask.dependsOn currentValidationTask
			}

			Task buildTask = project.tasks.findByName('build')

			project.fopRenderer.render.each { renderConfig ->

				Task currentTransformTask = project.tasks.create("transform${renderConfig.name.capitalize()}", XSLTTransformTask) { task ->
					task.renderConfig = renderConfig

					dependsOn validateAllTask
				}

				Task currentRenderTask = project.tasks.create("render${renderConfig.name.capitalize()}", FopRenderTask) { task ->
					baseDir = renderConfig.resourcesDir
					inputFiles = currentTransformTask.outputs.files
				}

				buildTask.dependsOn currentRenderTask
			}
		}
	}
}
