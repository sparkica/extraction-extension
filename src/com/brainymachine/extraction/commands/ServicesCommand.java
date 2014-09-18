package com.brainymachine.extraction.commands;

import javax.servlet.http.HttpServletRequest;

import com.brainymachine.extraction.commands.ElementExtractionCommand;
import com.brainymachine.extraction.services.ExtractionServiceManager;

import org.json.JSONArray;
import org.json.JSONWriter;


/**
 * Servlet that provides read/write access to <tt>ExtractionServiceManager</tt>
 * @author Mateja Verlic
 */
public class ServicesCommand extends ElementExtractionCommand {
    private final ExtractionServiceManager serviceManager;
    
    /**
     * Creates a new <tt>ServicesCommand</tt>
     * @param serviceManager The data source
     */
    public ServicesCommand(final ExtractionServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    /** {@inheritDoc} */
    @Override
    public void get(final HttpServletRequest request, final JSONWriter response) throws Exception {
        serviceManager.writeTo(response);
    }
    
    /** {@inheritDoc} */
    @Override
    public void put(final HttpServletRequest request, final Object body, final JSONWriter response) throws Exception {
        if(!(body instanceof JSONArray))
            throw new IllegalArgumentException("Body should be a JSON array.");
        serviceManager.updateFrom((JSONArray)body);
        serviceManager.save();
    }
}
