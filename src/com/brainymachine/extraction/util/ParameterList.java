package com.brainymachine.extraction.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * A list of name/value parameters
 */
public class ParameterList extends ArrayList<NameValuePair> {
    private static final long serialVersionUID = -3980790528494511180L;

    /**
     * @param name The name of pair
     * @param value The value of the pair
     */
    public void add(final String name, final String value) {
        add(new BasicNameValuePair(name, value));
    }
    
    /**
     * Converts the list to an URL encoded form entity
     * @return An entity
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public UrlEncodedFormEntity toEntity() throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(this);
    }
}
