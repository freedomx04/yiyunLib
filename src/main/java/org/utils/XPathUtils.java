package org.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathUtils {

	private static DocumentBuilderFactory documentBuilderFactory;
	public static DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		}
		return documentBuilderFactory;
	}

	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return getDocumentBuilderFactory().newDocumentBuilder();
	}

	private static DocumentBuilder documentBuilder;
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		if (documentBuilder == null) {
			documentBuilder = newDocumentBuilder();
		}
		return documentBuilder;
	}

	public static Document parseDocumentr(File f) throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder().parse(f);
	}

	public static Document parseDocumentr(InputSource is) throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder().parse(is);
	}

	public static Document parseDocumentr(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder().parse(is);
	}

	public static Document parseDocumentr(String uri) throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder().parse(uri);
	}

	public static Document parseDocumentr(InputStream is, String systemId) throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder().parse(is, systemId);
	}

	private static XPathFactory xpathFactory;
	public static XPathFactory getXPathFactory() {
		if (xpathFactory == null) {
			xpathFactory = XPathFactory.newInstance();
		}
		return xpathFactory;
	}

	public static Node selectSingleNode(Node contextNode, String expression) throws XPathExpressionException {
		XPath xpath = getXPathFactory().newXPath();
		XPathExpression xexpr = xpath.compile(expression);
		return (Node) xexpr.evaluate(contextNode, XPathConstants.NODE);
	}

	public static NodeList selectNodeList(Node contextNode, String expression) throws XPathExpressionException {
		XPath xpath = getXPathFactory().newXPath();
		XPathExpression xexpr = xpath.compile(expression);
		return (NodeList) xexpr.evaluate(contextNode, XPathConstants.NODESET);
	}

}
