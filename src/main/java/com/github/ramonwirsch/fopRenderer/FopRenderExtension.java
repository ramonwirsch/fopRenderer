package com.github.ramonwirsch.fopRenderer;

import groovy.lang.Closure;
import org.gradle.api.NamedDomainObjectContainer;

/**
 * Created by ramon on 13.11.2015.
 */
public class FopRenderExtension {

	private final NamedDomainObjectContainer<SchemaConfigExtension> schemas;
	private final NamedDomainObjectContainer<RenderConfigExtension> render;

	public FopRenderExtension(NamedDomainObjectContainer<SchemaConfigExtension> schemas, NamedDomainObjectContainer<RenderConfigExtension> render) {
		this.schemas = schemas;
		this.render = render;
	}

	public void schemas(Closure closure) {
		schemas.configure(closure);
	}

	public void render(Closure closure) {
		render.configure(closure);
	}

	public NamedDomainObjectContainer<SchemaConfigExtension> getSchemas() {
		return schemas;
	}

	public NamedDomainObjectContainer<RenderConfigExtension> getRender() {
		return render;
	}
}

