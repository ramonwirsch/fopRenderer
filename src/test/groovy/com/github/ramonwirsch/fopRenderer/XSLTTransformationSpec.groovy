package com.github.ramonwirsch.fopRenderer

import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class XSLTTransformationSpec extends Specification {

	@TempDir
	FileSystemFixture fsFixture
	
	File transformed
	
	def setup() {
		fsFixture.create {
			dir('xml') {
				copyFromClasspath('/data/xml/not-well-formed.xml')
				copyFromClasspath('/data/xml/well-formed-but-invalid.xml')
				copyFromClasspath('/data/xml/HAM.xml')
			}
			dir('xsl') {
				copyFromClasspath('/data/xsl/style1.xsl')
				copyFromClasspath('/data/xsl/xslt-version-test.xsl')
			}
			dir('xsd') {
				copyFromClasspath('/data/xsd/airport.xsd')
			}
		}
		
		transformed = fsFixture.file('out/transformed.xml').toFile()
		assert !transformed.exists()
	}
	
	def "successfully transforms a valid source" () {
		given: "a valid source"
		def src = fsFixture.resolve('xml/HAM.xml').toFile()
		assert src.canRead()
		
		and: "a valid stylesheet"
		def stylesheet = fsFixture.resolve('xsl/style1.xsl').toFile()
		assert stylesheet.canRead()
		
		and: "a target file which does not (yet) exist"
		assert !transformed.exists()

		and: "an instance without parameters"		
		def instance = new XSLTTransformation(stylesheet, [:])

		when: "transformation is invoked"
		instance.transform(src, transformed)

		then: "the result file exists"
		transformed.exists()
	}
	
	def "fails to transform a bad source"() {
		given: "a bad source"
		def src = fsFixture.resolve('xml/not-well-formed.xml').toFile()
		assert src.canRead()
		
		and: "a valid stylesheet"
		def stylesheet = fsFixture.resolve('xsl/style1.xsl').toFile()
		
		and: "an instance without parameters"
		def instance = new XSLTTransformation(stylesheet, [:])

		when: "transformation is invoked"
		instance.transform(src, transformed)

		then: "the transformation fails"
		Exception e = thrown()
		!transformed.exists()
	}

	def "fails to transform a valid source using a bad stylesheet"() {
		given: "a valid source"
		def src = fsFixture.resolve('xml/HAM.xml').toFile()
		assert src.canRead()
		
		and: "a bad stylesheet"
		def stylesheet = fsFixture.resolve('xml/not-well-formed.xml').toFile()
		
		and: "an instance without parameters"
		def instance = new XSLTTransformation(stylesheet, [:])

		when: "transformation is invoked"
		instance.transform(src, transformed)

		then: "the transformation fails"
		Exception e = thrown()
		!transformed.exists()
	}
	
	@PendingFeature
	@Issue("https://github.com/ramonwirsch/fopRenderer/issues/14")
	def "supports xslt 2 stylesheets"() {
		given: "a well-formed source"
		def src = fsFixture.resolve('xml/HAM.xml').toFile()
		assert src.canRead()
		
		and: "the version-test stylesheet"
		def stylesheet = fsFixture.resolve('xsl/xslt-version-test.xsl').toFile()
		
		and: "an instance without parameters"
		def instance = new XSLTTransformation(stylesheet, [:])

		when: "transformation is invoked"
		instance.transform(src, transformed)

		then: "the transformation succeeds"
		transformed.exists()
		fsFixture.resolve('out/transformed.xml').text == "3.0"
	}
	
}
