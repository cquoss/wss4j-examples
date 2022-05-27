package de.quoss.wss4j.examples;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.xml.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * 'borrowed' here:
     * https://github.com/apache/ws-wss4j/blob/0d29e0a1bfb722d216e45fc98b89b5efabfdcd93/ws-security-common/src/test/java/org/apache/wss4j/common/util/SOAPUtil.java#L29
     */
    private static final String SAMPLE_SOAP_MSG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<SOAP-ENV:Envelope "
                    + "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                    + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<SOAP-ENV:Body>"
                    + "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">"
                    + "<value xmlns=\"\">15</value>"
                    + "</add>"
                    + "</SOAP-ENV:Body>"
                    + "</SOAP-ENV:Envelope>";

    private void run() {
        final String methodName = "run()";
        WSSConfig.init();
        Crypto crypto;
        try {
            crypto = CryptoFactory.getInstance();
        } catch (WSSecurityException e) {
            throw new Wss4jExamplesException(e);
        }
        final Document doc = buildDocument();
        LOGGER.debug("{} [doc={}]", methodName, parseDom(doc));
        WSSecHeader header = new WSSecHeader(doc);
        try {
            header.insertSecurityHeader();
        } catch (WSSecurityException e) {
            throw new Wss4jExamplesException(e);
        }
        WSSecSignature signature = new WSSecSignature(header);
        signature.setUserInfo("foo", "bar");
        Document signedDoc;
        try {
            signedDoc = signature.build(crypto);
            LOGGER.debug("{} [signedDoc={}]", methodName, parseDom(signedDoc));
        } catch (WSSecurityException e) {
            throw new Wss4jExamplesException(e);
        }
        WSSecurityEngine engine = new WSSecurityEngine();
        RequestData data = new RequestData();
        data.setWssConfig(WSSConfig.getNewInstance());
        data.setSigVerCrypto(crypto);
        WSHandlerResult result;
        try {
            result = engine.processSecurityHeader(signedDoc, data);
        } catch (WSSecurityException e) {
            throw new Wss4jExamplesException(e);
        }
    }

    private Document buildDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Wss4jExamplesException(e);
        }
        try {
            return builder.parse(new ByteArrayInputStream(SAMPLE_SOAP_MSG.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException | SAXException e) {
            throw new Wss4jExamplesException(e);
        }
    }

    private String parseDom(final Document doc) {
        TransformerFactory factory = TransformerFactory.newDefaultInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new Wss4jExamplesException(e);
        }
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        try {
            transformer.transform(new DOMSource(doc), result);
        } catch (TransformerException e) {
            throw new Wss4jExamplesException(e);
        }
        return writer.toString();
    }

    public static void main(final String[] args) {
        new Main().run();
    }

}
