
package com.github.grimsa.restviasoap.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HttpMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HttpMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="GET"/&gt;
 *     &lt;enumeration value="POST"/&gt;
 *     &lt;enumeration value="PUT"/&gt;
 *     &lt;enumeration value="PATCH"/&gt;
 *     &lt;enumeration value="DELETE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "HttpMethod")
@XmlEnum
public enum HttpMethod {

    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    public String value() {
        return name();
    }

    public static HttpMethod fromValue(String v) {
        return valueOf(v);
    }

}
