
package com.github.grimsa.restviasoap.generated;

import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1
    extends XmlAdapter<String, URI>
{


    public URI unmarshal(String value) {
        return (java.net.URI.create(value));
    }

    public String marshal(URI value) {
        if (value == null) {
            return null;
        }
        return value.toASCIIString();
    }

}
