package com.github.ramonwirsch.fopRenderer

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner

import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import java.nio.file.Files

class FopRendererPluginSpec extends Specification {

	@TempDir
	FileSystemFixture fsFixture
	
	def setup() {
		fsFixture.create {
			dir('xml') {
				copyFromClasspath('/data/xml/not-well-formed.xml')
				copyFromClasspath('/data/xml/well-formed-but-invalid.xml')
				copyFromClasspath('/data/xml/HAM.xml')
			}
			dir('xsl') {
				copyFromClasspath('/data/xsl/style1.xsl')
			}
			dir('xsd') {
				copyFromClasspath('/data/xsd/airport.xsd')
			}
			fsFixture.file('build.gradle') << """
			plugins {
				id 'com.github.ramonwirsch.FopRenderer'
			}
			"""

		}
		
		
	}
	
	def "adds the fopRenderer extension to the project"() {
		given: "a fresh project without the plugin applied"
		def project = ProjectBuilder.builder().build()
		assert project.extensions.findByName('fopRenderer') == null
		
		when:
		project.pluginManager.apply("com.github.ramonwirsch.FopRenderer")
		
		then:
		project.extensions.findByName('fopRenderer') != null
	}

	def "adds the validate task to the project"() {
		given: "a fresh project without the plugin applied"
		def project = ProjectBuilder.builder().build()
		assert project.tasks.findByName('validate') == null
		
		when:
		project.pluginManager.apply("com.github.ramonwirsch.FopRenderer")
		
		then:
		project.tasks.findByName('validate') != null
	}		
	
	def "successfully validates a valid file"() {
		given:
		fsFixture.file('build.gradle') << """
		fopRenderer {
			schemas {
				'airports' {
					files = file("xml/HAM.xml")
					offlineSchema = file("xsd/airport.xsd")
				}
			}
		}
		"""
		
		when:
		def result = GradleRunner.create()
		.withProjectDir(fsFixture.getCurrentPath().toFile())
		.withArguments('validateAirports')
		.withPluginClasspath()
		.build()
		
		then:
		result.task(":validateAirports").outcome == SUCCESS
	}

	def "fails to validate an invalid file"() {
		given:
		fsFixture.file('build.gradle') << """
		fopRenderer {
			schemas {
				'airports' {
					files = file("xml/well-formed-but-invalid.xml")
					offlineSchema = file("xsd/airport.xsd")
				}
			}
		}
		"""
		
		when:
		def result = GradleRunner.create()
		.withProjectDir(fsFixture.getCurrentPath().toFile())
		.withArguments('validateAirports')
		.withPluginClasspath()
		.build()
		
		then:
		Exception e = thrown()
	}

	def "successfully renders a valid file"() {
		given:
		fsFixture.file('build.gradle') << """
		fopRenderer {
			render {
				'airports' {
					stylesheet = file("xsl/style1.xsl")
					rootSrc = file("xml/HAM.xml")
				}
			}
		}
		"""
		and: "output file does not yet exists"
		assert !Files.exists(fsFixture.resolve('build/doc/airports.pdf'))
		
		when:
		def result = GradleRunner.create()
		.withProjectDir(fsFixture.getCurrentPath().toFile())
		.withArguments('renderAirports')
		.withPluginClasspath()
		.build()
		
		then:
		result.task(":renderAirports").outcome == SUCCESS
		Files.exists(fsFixture.resolve('build/doc/airports.pdf'))
		
	}

}
