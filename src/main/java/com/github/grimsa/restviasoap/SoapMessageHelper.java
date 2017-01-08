package com.github.grimsa.restviasoap;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.github.grimsa.restviasoap.generated.Request;

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
}
