package nkp.pspValidator.shared.imageUtils.validation;

import nkp.pspValidator.shared.NamespaceContextImpl;
import nkp.pspValidator.shared.engine.exceptions.XmlParsingException;
import nkp.pspValidator.shared.imageUtils.ImageUtil;
import nkp.pspValidator.shared.imageUtils.ImageUtilManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by martin on 18.11.16.
 */
public class J2kXmlProfile extends J2kProfile {


    private final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

    public J2kXmlProfile(ImageUtilManager imageUtilManager, ImageUtil imageUtil) {
        super(imageUtilManager, imageUtil);
    }

    public void addNamespace(String prefix, String url) {
        namespaceContext.setNamespace(prefix, url);
    }

    public NamespaceContextImpl getNamespaceContext() {
        return namespaceContext;
    }

    Object processImageUtilOutput(String toolRawOutput, ImageUtil util) throws XmlParsingException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(toolRawOutput));
            Document doc = builder.parse(is);
            /*String docStr = XmlUtils.toString(doc);
            System.out.println(docStr);*/
            return doc;
        } catch (SAXException e) {
            throw new XmlParsingException(String.format("chyba parsování xml (výstup nástroje %s): %s", util, e.getMessage()));
        } catch (IOException e) {
            throw new XmlParsingException(String.format("chyba čtení výstupu nástroje %s: %s", util, e.getMessage()));
        } catch (ParserConfigurationException e) {
            throw new XmlParsingException(String.format("chyba konfigurace parseru při čtení výstupu nástroje %s: %s", util, e.getMessage()));
        }
    }

}