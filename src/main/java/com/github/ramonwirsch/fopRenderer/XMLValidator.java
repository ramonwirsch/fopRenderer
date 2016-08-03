/*
_______________________________________________________________________________

 Copyright (c) 2012 TU Dresden, Chair for Embedded Systems
 (http://www.mr.inf.tu-dresden.de) All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

 3. All advertising materials mentioning features or use of this software
    must display the following acknowledgement: "This product includes
    software developed by the TU Dresden Chair for Embedded Systems and
    its contributors."

 4. Neither the name of the TU Dresden Chair for Embedded Systems nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY TU DRESDEN CHAIR FOR EMBEDDED SYSTEMS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
_______________________________________________________________________________
*/

package com.github.ramonwirsch.fopRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.net.URL;


public class XMLValidator {

    private final Logger logger = LoggerFactory.getLogger(XMLValidator.class);
    private final URL schemaUri;
    private final Validator validator;
	private final SAXParser saxParser;

	public XMLValidator(URL schemaUri) throws SAXException, ParserConfigurationException {

        this.schemaUri = schemaUri;

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(this.schemaUri);

		validator = schema.newValidator();

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		saxParserFactory.setValidating(true);
		saxParserFactory.setXIncludeAware(true);
		saxParserFactory.setSchema(schema);
		saxParser = saxParserFactory.newSAXParser();
	}

	public boolean validate(File file) {

        try {
            logger.info("Parsing of " + file);

			saxParser.parse(file, new DefaultHandler());
            validator.validate(new StreamSource(file));

            logger.info("Successfully parsed {}", file);
            return true;
        } catch (SAXParseException saxe) {

            StringBuilder message = new StringBuilder();

            message.append("Validating ");
            message.append(file);
            message.append(" failed");

            int lineNumber = saxe.getLineNumber();
            int columnNumber = saxe.getColumnNumber();

            if (-1 != lineNumber) {
                message.append(" at line ");
                message.append(lineNumber);

                if (-1 != columnNumber) {
                    message.append(" column ");
                    message.append(columnNumber);
                }
            }

            message.append(": ");
            message.append(saxe.getMessage());

            logger.error(message.toString());
            return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
