package com.github.ramonwirsch.fopRenderer

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class SchemaConfigExtensionSpec extends Specification {

	Project project
	
	SchemaConfigExtension ext
	
	def setup() {
		project = ProjectBuilder.builder().build()
		ext = new SchemaConfigExtension("foo", project)
	}
	
	def "fails if no files are configured"() {
		when:
		ext.files
		
		then:
		ProjectConfigurationException e = thrown()
	}
	
	def "does not use inherent schemas per default"() {
		expect:
		!ext.useInherentSchemas
	}

	def "has no default offline schema"() {
		expect:
		ext.offlineSchema == null
	}
		
	def "uses the offline schema (if configured) as the schemaUri when no schemaUri is configured explicitly"() {
		given: "an offline schema configured"
		ext.offlineSchema = new File(project.buildDir, "offline.xsd")
		
		expect:
		ext.schemaUri == new File(project.buildDir, "offline.xsd").toURI().toURL()
	}
	
	def "uses the implicit as the schemaUri when neither a schemaUri nor an offline schema are configured explicitly"() {
		given: "no offline schema configured"
		assert ext.offlineSchema == null
		
		expect:
		ext.schemaUri == new URL("http://auto-implicit-validation")
	}

	def "uses the schemaUri when one is configured explicitly"() {
		given: "a schemaUri configured"
		ext.schemaUri = new URL("https://foo/bar.xsd")
		
		expect: "the schemaUri is the configured one"
		ext.schemaUri == new URL("https://foo/bar.xsd")
		
		when: "an offline schema is configured"
		ext.offlineSchema = new File("foo.xsd")
		
		then: "the schema is still the configured one"
		ext.schemaUri == new URL("https://foo/bar.xsd")
	}
		
}
