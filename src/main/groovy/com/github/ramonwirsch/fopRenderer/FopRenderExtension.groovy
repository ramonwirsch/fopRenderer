package com.github.ramonwirsch.fopRenderer

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * Created by ramon on 13.11.2015.
 */
class FopRenderExtension {

	final NamedDomainObjectContainer<SchemaConfigExtension> schemas
	final NamedDomainObjectCollection<RenderConfigExtension> render

	void schemas(Closure closure) {
		schemas.configure closure
	}

	void render(Closure closure) {
		render.configure closure
	}

	FopRenderExtension(NamedDomainObjectContainer<SchemaConfigExtension> schemas, NamedDomainObjectCollection<RenderConfigExtension> render) {
		this.schemas = schemas
		this.render = render
	}
}

class RenderConfigExtension {
	final String name

	File src

	File stylesheet

	File resourcesDir

	FileCollection dependencies

	RenderConfigExtension(String name) {
		this.name = name
	}
}

class SchemaConfigExtension {

	private static final FALLBACK_URL = new URL("http://auto-implicit-validation");

	final String name

	FileCollection files
	private final Project project

	void setFiles(File file) {
		files = project.files(file)
	}
	void setFiles(FileCollection files) {
		this.files = files
	}

	FileCollection getFiles() {
		files
	}

	File schemaDir = null;

	void setSchemaDir(File dir) {
		schemaDir = dir
	}

	URL schemaUri

	URL getSchemaUri() {
		if (schemaUri)
			return schemaUri;
		else
			return FALLBACK_URL;
	}

	void setSchemaUri(String uri) {
		schemaUri = new URL(uri)
	}

	void setSchemaUri(File file) {
		schemaUri = file.toURI().toURL();
	}

	void setSchemaUri(URL uri) {
		schemaUri = uri
	}

	File offlineSchema

	boolean useInherentSchemas = false

	SchemaConfigExtension(String name, Project project) {
		this.project = project
		this.name = name
	}
}
