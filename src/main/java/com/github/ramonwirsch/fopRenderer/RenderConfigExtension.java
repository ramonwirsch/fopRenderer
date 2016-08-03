package com.github.ramonwirsch.fopRenderer;

import com.github.ramonwirsch.fopRenderer.util.CollectionUtil;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.util.Map;

/**
 * Created by Ramon on 03.08.2016.
 */
public class RenderConfigExtension {
	private final String name;

	private File rootSrc;

	private File stylesheet;

	private FileCollection dependencies;

	private File resourcesBaseDir;

	private Map<String, Object> resourceCollectionParams = CollectionUtil.immutableMap("exclude", "**/*.xml");

	public RenderConfigExtension(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Root source file to render
	 *
	 * @return xml file
	 */
	public File getRootSrc() {
		return rootSrc;
	}

	/**
	 * Root source file to render
	 *
	 * @param src xml file
	 */
	public void setRootSrc(File src) {
		this.rootSrc = src;
	}

	/**
	 * directory, relative to which to find resources (graphics etc...)
	 * <p>
	 * Defaults to parent of {@link #getRootSrc()}
	 *
	 * @return directory
	 */
	public File getResourcesBaseDir() {
		return resourcesBaseDir != null ? resourcesBaseDir : rootSrc.getParentFile();
	}

	/**
	 * directory, relative to which to find resources (graphics etc...)
	 * <p>
	 * Defaults to parent of {@link #getRootSrc()}
	 *
	 * @param resourcesBaseDir directory
	 */
	public void setResourcesBaseDir(File resourcesBaseDir) {
		this.resourcesBaseDir = resourcesBaseDir;
	}

	/**
	 * Stylesheet to use
	 *
	 * @return xsd stylesheet
	 */
	public File getStylesheet() {
		return stylesheet;
	}

	/**
	 * Stylesheet to use
	 *
	 * @param stylesheet xsd stylesheet
	 */
	public void setStylesheet(File stylesheet) {
		this.stylesheet = stylesheet;
	}

	/**
	 * List of files xml files that are included by {@link #getRootSrc()} and whose changes require a new rendering
	 * <p>
	 * Defaults to all files in parent dir of {@link #getRootSrc()}
	 *
	 * @return collection of files
	 */
	public FileCollection getDependencies() {
		return dependencies;
	}

	/**
	 * List of files xml files that are included by {@link #getRootSrc()} and whose changes require a new rendering
	 * <p>
	 * Defaults to all files in parent dir of {@link #getRootSrc()}
	 *
	 * @param dependencies collection of files
	 */
	public void setDependencies(FileCollection dependencies) {
		this.dependencies = dependencies;
	}

	public Map<String, Object> getResourceCollectionParams() {
		return resourceCollectionParams;
	}

	public void setResourceCollectionParams(Map<String, Object> resourceCollectionParams) {
		this.resourceCollectionParams = resourceCollectionParams;
	}
}
