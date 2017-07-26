package com.iquanwai.util;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by justin on 14-7-24.
 */
public class XMLHelper {
    public static <T> String createXML(T t){
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jbc = JAXBContext.newInstance(t.getClass());   //传入要转换成xml的对象类型
            Marshaller mar = jbc.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
            mar.setProperty(Marshaller.JAXB_FRAGMENT, true);
            mar.marshal(t, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        String escaped_xml = sw.toString();

        return escaped_xml.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    public static <T> T parseXml(Class<T> clazz, String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller u = jc.createUnmarshaller();
            return (T) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String appendCDATA(String value){
        return "<![CDATA["+value+"]]>";
    }

    public static Document parseDocument(InputStream is) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            return doc;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    public static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    public static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
//            e.printStackTrace();
        }

        return null;
    }

    public static String getNode(Document document, String nodeName) {
        if (document == null) {
            return null;
        }
        Element element = document.getDocumentElement();
        NodeList list = element.getElementsByTagName(nodeName);
        if (list.getLength() == 0) {
            return null;
        }
        return list.item(0).getTextContent();
    }
}
