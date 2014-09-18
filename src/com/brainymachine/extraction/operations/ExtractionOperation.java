package com.brainymachine.extraction.operations;

import java.util.Properties;
import java.util.SortedMap;

import com.brainymachine.extraction.operations.ExtractionProcess;
import com.brainymachine.extraction.services.ExtractionService;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;


import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.operations.EngineDependentOperation;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.process.Process;
import com.google.refine.util.JSONUtilities;

/**
 * Operation that starts an element extraction process
 * @author Mateja Verlic
 */
public class ExtractionOperation extends EngineDependentOperation {
    private final Column column;
    private final SortedMap<String, ExtractionService> services;
    
    /**
     * Creates a new <tt>ExtractionOperation</tt>
     * @param column The column on which element extraction is performed
     * @param services The services that will be used for element extraction
     * @param engineConfig The faceted browsing engine configuration
     */
    public ExtractionOperation(final Column column, final SortedMap<String, ExtractionService> services, 
                    final JSONObject engineConfig) {
        super(engineConfig);
        this.column = column;
        this.services = services;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final JSONWriter writer, final Properties options) throws JSONException {
        writer.object();
        writer.key("op"); writer.value(OperationRegistry.s_opClassToName.get(getClass()));
        writer.key("description"); writer.value(getBriefDescription(null));
        writer.key("engineConfig"); writer.value(getEngineConfig());
        writer.key("column"); writer.value(column.getName());
        writer.key("services");
        JSONUtilities.writeStringArray(writer, services.keySet().toArray(new String[services.size()]));
        writer.endObject();
    }
    
    /** {@inheritDoc} */
    @Override
    protected String getBriefDescription(final Project project) {
        return String.format("Extract element value in column %s", column.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public Process createProcess(final Project project, final Properties options) throws Exception {
        return new ExtractionProcess(project, column, services, this, getBriefDescription(project), getEngineConfig());
    }
}
