package com.github.ramonwirsch.fopRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class XSLTTransformation {

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

	public boolean transform(File in, File out) {
		logger.info("Transforming {}", in);

		try {
//			DOMResult result=new DOMResult();
//			transformer.transform(
//					new DOMSource(documentBuilder.parse(new File(input))),
//					new StreamResult(new File(output)));
			
			transformer.transform(new DOMSource(documentBuilder.parse(in)),
					new StreamResult(out));

//			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
//
//			DOMImplementationLS impl =
//			    (DOMImplementationLS)registry.getDOMImplementation("LS");
//
//			LSSerializer writer = impl.createLSSerializer();
//			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
//			LSOutput outputLS = impl.createLSOutput();
//			FileOutputStream byteStream = new FileOutputStream(out);
//			outputLS.setByteStream(byteStream);
//			writer.write(result.getNode(), outputLS);
//
//			byteStream
			logger.info("Successfully transformed {}", out);
			return true;
		} catch (IOException | TransformerException | SAXException e) {
			logger.error("Transform Error", e);
			return false;
		}
	}
}
