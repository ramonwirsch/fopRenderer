package com.github.ramonwirsch.fopRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class XSLTTransformation implements ErrorListener {

	private static final Logger logger = LoggerFactory.getLogger(XSLTTransformation.class);
	private final File stylesheet;
	private final StreamSource stylesheetSource;
	private final Transformer transformer;
	private final DocumentBuilder documentBuilder;

	public XSLTTransformation(File stylesheet, Map<String, String> parameters) {
		this.stylesheet = stylesheet;
		this.stylesheetSource = new StreamSource(stylesheet);

		try {
			this.transformer = TransformerFactory.newInstance().newTransformer(stylesheetSource);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}

		for (Map.Entry<String,String> entry : parameters.entrySet()) {
			transformer.setParameter(entry.getKey(), entry.getValue());
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setXIncludeAware(true);
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void transform(File in, File out) throws TransformerException {
		logger.info("Transforming {}", in);
		transformer.setErrorListener(this);

		Document parse;
		try {
			parse = documentBuilder.parse(in);
		} catch (IOException | SAXException e) {
			throw new TransformerException(e);
		}

		transformer.transform(new DOMSource(parse),
				new StreamResult(out));

		logger.info("Successfully transformed {}", out);
	}

	@Override
	public void warning(TransformerException e) throws TransformerException {
		logger.warn("Transform Warning: {}", e.getMessageAndLocation());
	}

	@Override
	public void error(TransformerException e) throws TransformerException {
		throw e;

	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException {
		throw e;
	}
}
