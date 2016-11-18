package nkp.pspValidator.shared.imageUtils.validation.extractions;

import nkp.pspValidator.shared.imageUtils.ExtractionType;
import nkp.pspValidator.shared.imageUtils.validation.DataExtraction;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 17.11.16.
 */
public abstract class XmlDataExtraction implements DataExtraction {

    final ExtractionType extractionType;
    private final NamespaceContext namespaceContext;

    protected XmlDataExtraction(ExtractionType extractionType, NamespaceContext namespaceContext) {
        this.extractionType = extractionType;
        this.namespaceContext = namespaceContext;
    }

    XPathExpression buildXpath(String xpathExpression) throws ExtractionException {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(namespaceContext);
            return xpath.compile(xpathExpression);
        } catch (XPathExpressionException e) {
            throw new ExtractionException(String.format("chyba v zápisu Xpath '%s': %s", xpathExpression, e.getMessage()));
        }
    }

    Object extractData(XPathExpression xPath, Object processedOutput) throws XPathExpressionException {
        try {
            switch (extractionType) {
                case STRING: {
                    return xPath.evaluate(processedOutput, XPathConstants.STRING);
                }
                case INTEGER: {
                    String dataStr = (String) xPath.evaluate(processedOutput, XPathConstants.STRING);
                    if (dataStr == null) {
                        return null;
                    } else {
                        return Integer.valueOf(dataStr);
                    }
                }
                case FLOAT: {
                    String dataStr = (String) xPath.evaluate(processedOutput, XPathConstants.STRING);
                    if (dataStr == null) {
                        return null;
                    } else {
                        return Float.valueOf(dataStr);
                    }
                }
                case STRING_LIST: {
                    NodeList nodes = (NodeList) xPath.evaluate(processedOutput, XPathConstants.NODESET);
                    if (nodes == null) {
                        return null;
                    } else {
                        List<String> result = new ArrayList<>(nodes.getLength());
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node node = nodes.item(i);
                            result.add(node.getTextContent());
                        }
                        return result;
                    }
                }
                case INTEGER_LIST: {
                    NodeList nodes = (NodeList) xPath.evaluate(processedOutput, XPathConstants.NODESET);
                    if (nodes == null) {
                        return null;
                    } else {
                        List<Integer> result = new ArrayList<>(nodes.getLength());
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node node = nodes.item(i);
                            result.add(Integer.valueOf(node.getTextContent()));
                        }
                        return result;
                    }
                }
                case FLOAT_LIST: {
                    NodeList nodes = (NodeList) xPath.evaluate(processedOutput, XPathConstants.NODESET);
                    if (nodes == null) {
                        return null;
                    } else {
                        List<Float> result = new ArrayList<>(nodes.getLength());
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node node = nodes.item(i);
                            result.add(Float.valueOf(node.getTextContent()));
                        }
                        return result;
                    }
                }
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}