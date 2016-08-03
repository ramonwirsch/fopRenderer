package com.github.ramonwirsch.fopRenderer;

import com.github.ramonwirsch.fopRenderer.util.CollectionUtil;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;

/**
 * Created by ramon on 13.11.2015.
 */
public class FopRendererPlugin implements Plugin<Project> {

	static boolean isOffline(Project project) {
		if (project.getGradle().getStartParameter().isOffline()) {
			return true;
		} else if (project.getProperties().containsKey("offlineSchemas")) {
			Object offlineSchemas = project.getProperties().get("offlineSchemas");
			if (offlineSchemas == null)
				return false;

			return Boolean.valueOf(offlineSchemas.toString());
		} else {
			return false;
		}
	}

	@Override
	public void apply(Project project) {
		project.apply(CollectionUtil.immutableMap("plugin", "base"));

		NamedDomainObjectContainer<SchemaConfigExtension> schemas = project.container(SchemaConfigExtension.class, name -> new SchemaConfigExtension(name, project));

		NamedDomainObjectContainer<RenderConfigExtension> render = project.container(RenderConfigExtension.class);

		FopRenderExtension fopRenderer = project.getExtensions().create("fopRenderer", FopRenderExtension.class, schemas, render);

		TaskContainer tasks = project.getTasks();

		Task validateAllTask = tasks.create("validate", it -> {
			it.setGroup("verification");
			it.setDescription("Validate all XML files");
		});

		tasks.findByName("check").dependsOn(validateAllTask);

		project.afterEvaluate(it -> {
			fopRenderer.getSchemas().forEach(schemaConfig -> {
				Task currentValidationTask = tasks.create("validate" + StringGroovyMethods.capitalize(schemaConfig.getName()), XMLValidatorTask.class, task -> {
					task.setSchemaConfig(schemaConfig);
				});

				validateAllTask.dependsOn(currentValidationTask);
			});

			Task buildTask = tasks.findByName("build");

			fopRenderer.getRender().forEach(renderConfig -> {

				XSLTTransformTask currentTransformTask = tasks.create("transform" + StringGroovyMethods.capitalize(renderConfig.getName()), XSLTTransformTask.class, task -> {
					task.setRenderConfig(renderConfig);
					task.dependsOn(validateAllTask);
				});

				FopRenderTask currentRenderTask = tasks.create("render" + StringGroovyMethods.capitalize(renderConfig.getName()), FopRenderTask.class, task -> {
					task.setRenderConfig(renderConfig);
					task.setInput(currentTransformTask.getOutputFile());
					task.dependsOn(currentTransformTask);
				});

				buildTask.dependsOn(currentRenderTask);
			});
		});
	}
}
