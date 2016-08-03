package com.github.ramonwirsch.fopRenderer;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ramon on 03.08.2016.
 */
public class SchemaConfigExtension {

	/**
	 * Placeholder to configure plugin for implicit schemas (the ones that are configured within the xml files themselves)
	 */
	public static final URL FALLBACK_URL;

	static {
		try {
			FALLBACK_URL = new URL("http://auto-implicit-validation");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private final String name;
	private final Project project;
	private FileCollection files;
	private File schemaDir = null;
	private URL schemaUri;
	private File offlineSchema;
	private boolean useInherentSchemas = false;

	public SchemaConfigExtension(String name, Project project) {
		this.project = project;
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public String getName() {

		return name;
	}

	/**
	 * Directory containing all the schemas
	 *
	 * @return schema directory
	 */
	public File getSchemaDir() {
		return schemaDir;
	}

	/**
	 * Directory containing all the schemas
	 *
	 * @param dir schema directory
	 */
	public void setSchemaDir(File dir) {
		schemaDir = dir;
	}

	/**
	 * Local backup location for schemas
	 *
	 * @return parent dir of offline schemas
	 */
	public File getOfflineSchema() {
		return offlineSchema;
	}

	/**
	 * Local backup location for schemas
	 *
	 * @param offlineSchema parent dir of offline schemas
	 */
	public void setOfflineSchema(File offlineSchema) {
		this.offlineSchema = offlineSchema;
	}

	/**
	 * force use of XML schemas configured inside source files
	 *
	 * @return if enabled
	 */
	public boolean isUseInherentSchemas() {
		return useInherentSchemas;
	}

	/**
	 * force use of XML schemas configured inside source files
	 *
	 * @param useInherentSchemas boolean
	 */
	public void setUseInherentSchemas(boolean useInherentSchemas) {
		this.useInherentSchemas = useInherentSchemas;
	}

	/**
	 * Files to validate
	 *
	 * @param file single file to validate
	 */
	public void setFiles(File file) {
		files = project.files(file);
	}

	/**
	 * Files to validate
	 *
	 * @return files to validate
	 */
	public FileCollection getFiles() {
		return files;
	}

	/**
	 * Files to validate
	 *
	 * @param files files to validate
	 */
	public void setFiles(FileCollection files) {
		this.files = files;
	}

	/**
	 * Get the configured schema
	 * Defaults to implicit schemas
	 *
	 * @return schema file, can be URL
	 */
	public URL getSchemaUri() {
		if (schemaUri != null)
			return schemaUri;
		else
			return FALLBACK_URL;
	}

	/**
	 * Set the schema to use for validation
	 * Defaults to implicit schemas
	 *
	 * @param file local schema file
	 * @throws MalformedURLException in case of invalid URL
	 */
	public void setSchemaUri(File file) throws MalformedURLException {
		schemaUri = file.toURI().toURL();
	}

	/**
	 * Set the schema to use for validation
	 * Defaults to implicit schemas
	 *
	 * @param uri schema URL
	 */
	public void setSchemaUri(URL uri) {
		schemaUri = uri;
	}

	/**
	 * Set the schema to use for validation
	 * Defaults to implicit schemas
	 *
	 * @param uri schema URL
	 * @throws MalformedURLException in case of invalid URL
	 */
	public void setSchemaUri(String uri) throws MalformedURLException {
		schemaUri = new URL(uri);
	}
}
