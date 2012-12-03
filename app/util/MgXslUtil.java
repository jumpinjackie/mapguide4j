package util;

import play.*;
import org.osgeo.mapguide.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// For write operation
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

public class MgXslUtil
{
    public static String TransformByteReader(MgByteReader byteReader, String xslStylesheet, Map<String, String> xslParameters) throws MgException {
        String retVal = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            InputStream xslStream = Play.application().classloader().getResourceAsStream("resources/" + xslStylesheet);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(MgAjaxViewerUtil.ByteReaderToStream(byteReader));

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(xslStream);
            Transformer transformer = tFactory.newTransformer(stylesource);

            if (xslParameters != null) {
                for (String key : xslParameters.keySet()) {
                    transformer.setParameter(key, xslParameters.get(key));
                }
            }

            DOMSource source = new DOMSource(document);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);
            transformer.transform(source, result);

            retVal = baos.toString();
        }
        catch (TransformerException ex) { //I don't care
            UncheckedThrow.throwUnchecked(ex);
        }
        catch (ParserConfigurationException ex) { //I don't care
            UncheckedThrow.throwUnchecked(ex);
        }
        catch (SAXException ex) { //I don't care
            UncheckedThrow.throwUnchecked(ex);
        }
        catch (IOException ex) { //I don't care
            UncheckedThrow.throwUnchecked(ex);
        }

        return retVal;
    }
}