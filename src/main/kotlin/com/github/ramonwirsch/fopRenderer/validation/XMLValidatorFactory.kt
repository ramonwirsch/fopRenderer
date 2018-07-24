package com.github.ramonwirsch.fopRenderer.validation

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.net.URL
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

class XMLValidatorFactory
@Throws(SAXException::class, ParserConfigurationException::class) constructor(
		schemaUri: URL?
) {
	val schema: Schema
	val saxParserFactory: SAXParserFactory

	init {
		val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
		schema = if (schemaUri == null) schemaFactory.newSchema() else schemaFactory.newSchema(schemaUri)
		saxParserFactory = SAXParserFactory.newInstance()
		saxParserFactory.isNamespaceAware = true
		saxParserFactory.isValidating = true
		saxParserFactory.isXIncludeAware = true
		saxParserFactory.schema = schema
	}

	fun createValidator(): XMLValidator {
		val validator = schema.newValidator()

		val saxParser = saxParserFactory.newSAXParser()

		return XMLValidator(saxParser, validator)
	}

	companion object {
		val cache = WeakHashMap<URL?, XMLValidatorFactory>()

		fun forSchema(schemaUri: URL?): XMLValidatorFactory = cache.computeIfAbsent(schemaUri) { XMLValidatorFactory(it) }
	}
}

class XMLValidator(
		val saxParser: SAXParser,
		val validator: Validator
) {
	companion object {
		private val logger: Logger = Logging.getLogger(XMLValidator::class.java)
	}

	fun validateOrThrow(file: File): Boolean {
		try {
			logger.info("Parsing of " + file)

			saxParser.parse(file, DefaultHandler())
			validator.validate(StreamSource(file))

			logger.info("Successfully parsed {}", file)
			return true
		} catch (saxe: SAXParseException) {

			val messageBuilder = StringBuilder()

			messageBuilder.append("Validating ")
			messageBuilder.append(file)
			messageBuilder.append(" failed")

			val lineNumber = saxe.lineNumber
			val columnNumber = saxe.columnNumber

			if (-1 != lineNumber) {
				messageBuilder.append(" at line ")
				messageBuilder.append(lineNumber)

				if (-1 != columnNumber) {
					messageBuilder.append(" column ")
					messageBuilder.append(columnNumber)
				}
			}

			messageBuilder.append(": ")
			messageBuilder.append(saxe.message)

			val message = messageBuilder.toString()
			throw RuntimeException(message, saxe)
		} catch (e: Exception) {
			throw RuntimeException(e)
		}

	}
}