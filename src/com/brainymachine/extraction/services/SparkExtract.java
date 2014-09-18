package com.brainymachine.extraction.services;

import static com.brainymachine.extraction.util.UriUtil.createUri;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;

import com.brainymachine.extraction.services.ExtractionService;
import com.brainymachine.extraction.services.ExtractionServiceBase;
import com.brainymachine.extraction.util.ParameterList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * 
 * @author Mateja Verlic
 */
public class SparkExtract extends ExtractionServiceBase implements ExtractionService {
    private final static URI SERVICEBASEURL = createUri("http://localhost:5000/extract");
    private final static String[] PROPERTYNAMES = { "xpath","attribute","column" };

    /**
     * Creates a new Extraction service connector
     */
    public SparkExtract() {
        super(SERVICEBASEURL, PROPERTYNAMES);
        setProperty("xpath", "//title"); //default
        setProperty("attribute", ""); //default
        setProperty("column", ""); //default
    }
    
    /** {@inheritDoc} */
    protected HttpEntity createExtractionRequestBody(final String url) throws UnsupportedEncodingException {
        final ParameterList parameters = new ParameterList();
        parameters.add("xpath", getProperty("xpath"));
        parameters.add("attribute", getProperty("attribute"));
        parameters.add("url", url);
        return parameters.toEntity();
    }
    
    /** {@inheritDoc} */
    @Override
    protected String[] parseExtractionResponseElement(final JSONTokener tokener) throws JSONException {
            
        String response_string = (String)tokener.nextValue();
        final JSONObject response = new JSONObject(response_string);
        // Empty result if no resources were found
        if (!response.has("elements"))
            return EMPTY_EXTRACTION_RESULT;
        // Extract resources
        final JSONArray elements = response.getJSONArray("elements");
        final String[] results = new String[elements.length()];
        for (int i = 0; i < elements.length(); i++) {
            final JSONObject element = elements.getJSONObject(i);
            results[i] = new String(element.getString("value"));
        }
        return results;
    }
}
