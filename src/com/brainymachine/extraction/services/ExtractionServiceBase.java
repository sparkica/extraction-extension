package com.brainymachine.extraction.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brainymachine.extraction.services.ExtractionService;

import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONWriter;

/**
 * Abstract base class for element extraction services
 * with default support for JSON communication
 * @author Mateja Verlic
 */
public abstract class ExtractionServiceBase implements ExtractionService {
    /** The empty extraction result, containing no entities. */
    protected final static String[] EMPTY_EXTRACTION_RESULT = new String[0];
    
    private final static Charset UTF8 = Charset.forName("UTF-8");
    
    
    private URI serviceUrl;
    private final String[] propertyNames;
    private final HashMap<String, String> properties;
    
    /**
     * Creates a new element extraction service base class
     * @param propertyNames The names of supported properties
     */
    public ExtractionServiceBase(final String[] propertyNames) {
        this(null, propertyNames);
    }
        
    /**
     * Creates a new element extraction service base class
     * @param serviceUrl The URL of the service (can be null if not fixed)
     * @param propertyNames The names of supported properties
     * @param documentationUri The URI of the service's documentation
     */
    public ExtractionServiceBase(final URI serviceUrl, final String[] propertyNames) {
        this.serviceUrl = serviceUrl;
        this.propertyNames = propertyNames;
        
        properties = new HashMap<String, String>(propertyNames.length);
        for (String propertyName : propertyNames)
            this.properties.put(propertyName, "");
    }
    
    public void setServiceUrl(String serviceUrl) {
            this.serviceUrl= this.serviceUrl.resolve(serviceUrl);
    }
    
    /** {@inheritDoc} */
    @Override
    public String[] getPropertyNames() {
        return propertyNames;
    }

    /** {@inheritDoc} */
    @Override
    public String getProperty(final String name) {
        return properties.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public void setProperty(final String name, final String value) {
        if (!properties.containsKey(name))
            throw new IllegalArgumentException("The property " + name
                                               + " is invalid for " + getClass().getName() + ".");
        properties.put(name, value == null ? "" : value);
    }
    
    /** {@inheritDoc} */
    @Override
    public String[] extractElementValues(final String url) throws Exception {
        final HttpUriRequest request = createExtractionRequest(url);
        return performExtractionRequest(request);
    }
    
    /** {@inheritDoc} */
    public boolean isConfigured() {
        return true;
    }
    
    /**
     * Performs the element extraction request
     * @param request The request
     * @return The extracted elements
     * @throws Exception if the request fails
     */
    protected String[] performExtractionRequest(final HttpUriRequest request) throws Exception {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        final HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            throw new IllegalStateException(
                    String.format("The extraction request returned status code %d instead of %s.",
                                  response.getStatusLine().getStatusCode(), HttpStatus.SC_OK));
        final HttpEntity responseElement = response.getEntity();
        return parseExtractionResponseElement(responseElement);
    }

    /**
     * Creates a element extraction request on the specified text
     * @param text The text to analyze
     * @return The created request
     * @throws Exception if the request cannot be created
     */
    protected HttpUriRequest createExtractionRequest(final String text) throws Exception {
        final URI requestUrl = createExtractionRequestUrl(text);
        final HttpEntity body = createExtractionRequestBody(text);
        final HttpPost request = new HttpPost(requestUrl);
        request.setHeader("Accept", "application/json");
        request.setEntity(body);
        return request;
    }
    
    /**
     * Creates the URL for a element extraction request on the specified text
     * @param text The text to analyze
     * @return The created URL
     */
    protected URI createExtractionRequestUrl(final String text) {
        return serviceUrl;
    }

    
    
    /**
     * Creates the body for a element extraction request on the specified text
     * @param text The text to analyze
     * @return The created body entity
     * @throws Exception if the request body cannot be created
     */
    protected HttpEntity createExtractionRequestBody(final String text) throws Exception {
        final ByteArrayOutputStream bodyOutput = new ByteArrayOutputStream();
        final JSONWriter bodyWriter = new JSONWriter(new OutputStreamWriter(bodyOutput, UTF8));
        try {
            writeExtractionRequestBody(text, bodyWriter);
        }
        catch (JSONException error) {
            throw new RuntimeException(error);
        }
        try {
            bodyOutput.close();
        }
        catch (IOException e) { }
        final byte[] bodyBytes = bodyOutput.toByteArray();
        final ByteArrayInputStream bodyInput = new ByteArrayInputStream(bodyBytes);
        final HttpEntity body = new InputStreamEntity(bodyInput, bodyBytes.length);
        return body;
    }
    
    /**
     * Writes the body JSON for a element extraction request on the specified text
     * @param text The text to analyze
     * @param body The body writer
     * @throws JSONException if writing the body goes wrong
     */
    protected void writeExtractionRequestBody(final String text, final JSONWriter body) throws JSONException { }
    
    /**
     * Parses the entity of the element extraction response
     * @param response A response of the extraction service
     * @return The extracted elements
     * @throws Exception if the response cannot be parsed
     */
    protected String[] parseExtractionResponseElement(HttpEntity response) throws Exception {
        final InputStreamReader responseReader = new InputStreamReader(response.getContent());
        return parseExtractionResponseElement(new JSONTokener(responseReader));
    }
    
    /**
     * Parses the JSON entity of the element extraction response
     * @param tokener The tokener containing the response
     * @return The extracted elements
     * @throws JSONException if the response cannot be parsed
     */
    protected String[] parseExtractionResponseElement(final JSONTokener tokener) throws JSONException {
        return EMPTY_EXTRACTION_RESULT;
    }
    
    /**
     * Encodes the specified text for use in an URL.
     * @param text The text to encode
     * @return The encoded text
     */
    protected static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException error) {
            throw new RuntimeException(error);
        }
    }
}
