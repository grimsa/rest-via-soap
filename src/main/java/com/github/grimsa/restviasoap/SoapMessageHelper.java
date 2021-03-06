package com.github.grimsa.restviasoap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import com.github.grimsa.restviasoap.generated.Request;
import com.github.grimsa.restviasoap.generated.Response;

class SoapMessageHelper {

    Request readRequest(InputStream soapMessageInputStream) {
        try {
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, soapMessageInputStream);
            Unmarshaller unmarshaller = JAXBContext.newInstance(Request.class).createUnmarshaller();
            return (Request) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
        } catch (JAXBException | SOAPException | IOException e) {
            throw new RuntimeException("Failed to extract Request from SOAP message", e);
        }
    }

    void writeResponse(Response response, OutputStream outputStream) {
        try {
            Document soapBodyContent = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            JAXBContext context = JAXBContext.newInstance(Response.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            m.marshal(response, soapBodyContent);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPBody().addDocument(soapBodyContent);
            soapMessage.writeTo(outputStream);

        } catch (ParserConfigurationException | JAXBException | IOException | SOAPException e) {
            throw new RuntimeException("Failed to write Response as SOAP message", e);
        }
    }
}
