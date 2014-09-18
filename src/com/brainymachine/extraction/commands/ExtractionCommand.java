package com.brainymachine.extraction.commands;

import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.brainymachine.extraction.operations.ExtractionOperation;
import com.brainymachine.extraction.services.ExtractionService;
import com.brainymachine.extraction.services.ExtractionServiceManager;

import com.google.refine.commands.EngineDependentCommand;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Column;
import com.google.refine.model.Project;

/**
 * Command that starts extraction operation
 * @author Mateja Verlic
 */
public class ExtractionCommand extends EngineDependentCommand {
    private final ExtractionServiceManager serviceManager;
    
    /**
     * Creates a new <tt>ExtractionCommand</tt>
     * @param serviceManager The manager whose services will be used for extraction
     */
    public ExtractionCommand(final ExtractionServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractOperation createOperation(Project project, HttpServletRequest request, JSONObject engineConfig) throws Exception {
        final String columnName = request.getParameter("column");
        final Column column = project.columnModel.getColumnByName(columnName);
        final String[] serviceNames = request.getParameterValues("services[]");
        final TreeMap<String, ExtractionService> services = new TreeMap<String, ExtractionService>();
        for (String serviceName : serviceNames)
            services.put(serviceName, serviceManager.getService(serviceName));
        
        return new ExtractionOperation(column, services, getEngineConfig(request));
    }
}
